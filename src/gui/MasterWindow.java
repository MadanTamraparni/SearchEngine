package gui;

import javax.swing.JButton;
import javax.swing.JFrame;

public class MasterWindow extends JFrame{
    private static final long serialVersionUID = 1L;
    public static void main(String[] args){
        new MasterWindow();
    }

    private MasterWindow(){
        super("Seach Engine");
        JFrame mainFrame = new JFrame("Search Frame");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton searchButton = new JButton("Search");
        mainFrame.add(searchButton);

        setSize(600,600);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

    }

}

