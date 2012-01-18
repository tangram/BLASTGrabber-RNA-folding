package RNAFolding;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FoldGraphic extends JFrame {
	private static final long serialVersionUID = -3069890720770045627L;
	
	String sequence;
	ArrayList<Hairpin> hairpins;
	
	public FoldGraphic(HairpinStructure structure) {
		this.sequence = structure.dnaSequence;
		hairpins = structure.hairpins;
		
		createWindow();
	}
        
        private void createWindow(){
        GraphicsPanel panel = new GraphicsPanel();
        setLocationByPlatform(true);
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(panel);
        setVisible(true);
	}
        

	private class GraphicsPanel extends JPanel {
		private static final long serialVersionUID = 7231696028399642624L;

		@Override
		public void paint(Graphics g) {			
			Hairpin hairpin = hairpins.get(0);
			Graphics2D g2d = (Graphics2D)g;
			
			Point point;
			int startIndex = hairpin.startIndex;
			
			int i = startIndex;			//"manual" iterator
			int tempIndex;
			point = new Point(50, 50);
			
			//drawing top strand
			tempIndex = hairpin.topStrandLength;			//moving tempIndex to end of top strand
			while(i < tempIndex) {
				drawBase(g2d, sequence.substring(i, i+1), point);
				point.y += 30;
				i++;
			}
			
			point.x += 21; 		//moving point 45 degrees in x-direction (30cos45)
			point.y -= 9; 		//moving point back to 30sin45 relative to last nucleotide, mind our while-loop already moved it 30 down			
			
			//drawing top helix
			while(i < tempIndex + hairpin.helixTop.length()) {
				drawBase(g2d, sequence.substring(i, i+1), point);
				point.x += 30;
				i++;
			}
			
			point.x -= 30; 		//resetting the point back to the last nucleotide
			tempIndex += hairpin.helixTop.length();		//moving tempIndex to start of loop

			double loopAngle = Math.PI - (Math.PI * (hairpin.loopLength)) / (hairpin.loopLength + 2);
			double tempAngle = Math.PI/2 - loopAngle;
			
			//placing point at first nucleotide in the loop
			point.x += (int)(30 * Math.cos(tempAngle));
			point.y -= (int)(30 * Math.sin(tempAngle));
						
			//drawing first nucleotide in loop
			drawBase(g2d, sequence.substring(i, i+1), point);
			
			//drawing rest of the loop
			while(i < tempIndex + hairpin.loopLength - 1) {
				i++;
				tempAngle -= loopAngle;
				point.x += (int)(30 * Math.cos(tempAngle));
				point.y -= (int)(30 * Math.sin(tempAngle));
				drawBase(g2d, sequence.substring(i, i+1), point);
			}
			
			tempIndex += hairpin.loopLength;		//moving tempIndex to start of bottom helix
			
			//placing the point at beginning (end) of bottom helix
			tempAngle -= loopAngle;
			point.x += (int)(30 * Math.cos(tempAngle));
			point.y -= (int)(30 * Math.sin(tempAngle));
			
			//drawing bottom helix
			while(i < tempIndex + hairpin.helixBottom.length() - 1) {
				i++;
				drawBase(g2d, sequence.substring(i, i+1), point);
				point.x -= 30;
			}
			
			tempIndex += hairpin.helixBottom.length();		//moving tempIndex to start of strand
			
			//drawing first nucleotide in bottom strand 45 degrees away from and below the last (first) nucleotide in bottom helix
			i++;
			point.x += 9;
			point.y += 21;
			drawBase(g2d, sequence.substring(i, i+1), point);
			
			//drawing the rest of the bottom strand
			while(i < tempIndex + hairpin.bottomStrandLength - 1) {
				i++;
				point.y += 30;
				drawBase(g2d, sequence.substring(i, i+1), point);
			}
			
			//TODO make lines between nucleotides
			//TODO Draw "loose" nucleiosides
			//TODO Connect several hairpins (eventually)
		}
		
		private void drawBase(Graphics2D g, String c, Point point) {
			g.setColor(c.equals("A") ? Color.BLUE : c.equals("U") ? Color.GREEN : c.equals("G") ? Color.RED : Color.MAGENTA);
			g.fillOval(point.x, point.y, 20, 20);
			g.setColor(Color.WHITE);
			g.setFont(g.getFont().deriveFont(Font.BOLD, 17.0f));
			g.drawString(c, point.x+5, point.y+16);
		}
	}


}