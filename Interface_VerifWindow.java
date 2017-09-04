import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Interface_VerifWindow {

	public void ExitQuestion(final JFrame window){
		 window.setLayout(new GridLayout(2, 1));
		 JPanel test = new JPanel();
		 test.setLayout(new GridLayout(1, 2));
		 JButton choice1 = new JButton("Relancer");
		 JButton choice2 = new JButton("Quitter");
		    
		 choice1.addActionListener(new ActionListener(){
		       public void actionPerformed(ActionEvent event){
		    	   new Interface_AppliChoice();
		    	   window.dispose();
		        }
		      });
		    
		    choice2.addActionListener(new ActionListener(){
		        public void actionPerformed(ActionEvent event){
		        	window.dispose();
		        }
		      });
		    
		   test.add(choice1);
		   test.add(choice2);
		   window.add(test);
		   window.setVisible(true);
	}
	
}
