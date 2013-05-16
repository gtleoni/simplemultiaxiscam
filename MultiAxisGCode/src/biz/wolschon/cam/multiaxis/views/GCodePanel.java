package biz.wolschon.cam.multiaxis.views;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JList;

import biz.wolschon.cam.multiaxis.model.IModel;
import biz.wolschon.cam.multiaxis.strategy.FollowSurfaceNormalCutStrategy;
import biz.wolschon.cam.multiaxis.strategy.GCodeWriterStrategy;
import biz.wolschon.cam.multiaxis.strategy.IStrategy;
import biz.wolschon.cam.multiaxis.strategy.LinearStrategy;
import biz.wolschon.cam.multiaxis.strategy.StraightZCutStrategy;
import biz.wolschon.cam.multiaxis.trigonometry.Axis;
import biz.wolschon.cam.multiaxis.tools.BallShape;
import biz.wolschon.cam.multiaxis.tools.Tool;

/**
 * Panel to start G-Code generation and show the generated G-Code.
 */
public class GCodePanel extends JPanel {

	/**
	 * Helper-class for the ListModel.
	 * Wraps a line of G-Code to display and the corresponding tool-location.
	 */
	private static class GCodeLine {
		private String mLine;
		private double[] mToolLocation;
		public GCodeLine (final String aLine, final double[] aToolLocation) {
			this.mLine = aLine;
			this.mToolLocation = aToolLocation;
		}
		public double[] getToolLocation() {
			return mToolLocation;
		}
		@Override
		public String toString() {
			return mLine;
		}
	}

	/**
	 * The 3D model we are working with.	
	 */
	private IModel mModel;
	private JList codeList;
	private DefaultListModel codeListModel;
	private StrategyCreationPanel mStrategyPanel;
	private ModelReviewPanel mReviewTab;
	/**
	 * The tool we use Currently fixed to a 5.0mm ball nose cutter.
	 */
	private Tool mTool = new Tool(5.0d);

	/**
	 * Create the panel.<br/>
	 * If a ModelReviewPanel is given, a click inside the g-code list will show that tool position in the ModelReviewPanel.
	 * @param reviewTab may be null
	 */
	public GCodePanel(final IModel aModel, final ModelReviewPanel aReviewTab, final StrategyCreationPanel aStrategyPanel) {
		this.mModel = aModel;
		this.mReviewTab = aReviewTab;
		this.mStrategyPanel = aStrategyPanel;
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		final JButton btnGenerateGcode = new JButton("Generate G-Code");
		scrollPane.setColumnHeaderView(btnGenerateGcode);
		btnGenerateGcode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnGenerateGcode.setEnabled(false);
				Thread t = new Thread() {
					public void run() {
						onGenerateGCode();
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								btnGenerateGcode.setEnabled(true);
							}
						});
					}
				};
				t.start();
				
				
			}
		});
		
		codeList = new JList();
		codeListModel = new DefaultListModel();
		codeList.setModel(codeListModel);
		codeList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				GCodeLine line = (GCodeLine) codeList.getSelectedValue();
				mReviewTab.setTool(mTool);
				mReviewTab.setToolLocation(line.getToolLocation());
				
			}
		});
		scrollPane.setViewportView(codeList);
	}

	protected void onGenerateGCode() {
		try {
			FileWriter outfile = new FileWriter("/tmp/out.gcode");
			mTool = mStrategyPanel.getTool();
			GCodeWriterStrategy out = new GCodeWriterStrategy(outfile) {
				@Override
				protected void writeCodeLine(final String aLine, final double[] aLocation) throws IOException {
					super.writeCodeLine(aLine, aLocation);
					codeListModel.addElement(new GCodeLine(aLine, aLocation));
				}
			};
			IStrategy strategy = mStrategyPanel.getStrategy(out, mModel);
			try {
				double[] startLocation = new double[] {
						mModel.getCenterX(),
						mModel.getMinY(),
						mModel.getCenterZ(),
						0 // A axis
						// no B axis
				};
				strategy.runStrategy(startLocation);
				strategy.endStrategy();
			} finally {
				outfile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}