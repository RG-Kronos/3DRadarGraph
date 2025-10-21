import java.awt.Color; 
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.BasicStroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

import java.util.ArrayList;

public class GraphWindow extends JPanel implements KeyListener, MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = 1811198275293485774L;

	JFrame frame=new JFrame();
	JPanel main_panel=new JPanel(); // contains graphing panel and data panel
	JPanel data_panel=new JPanel(); // data panel displying all values of parameters
	
	JPanel data_value_panel=new JPanel();
	JPanel data_radial_panel=new JPanel();
	
	// "this" (extended JPanel) is the graphing panel on which drawing/graphing occurs
	
	/* 
	 * Constants
	 */
	final double pi=Math.PI;
	final int x=0, y=1, z=2;
	final int opacity=100;
	final int da=5;
	
	/* 
	 * Components
	 */
	int frameHeight=0, frameWidth=0;
	int cframeHeight=0, cframeWidth=0;
	
	int mpWidth=1385, mpHeight=840; 
	Dimension dimMP=new Dimension(mpWidth, mpHeight);
	
	Dimension dim1=new Dimension(mpWidth/2, mpHeight); // 1/2 panel dimensions

	Dimension dim2=new Dimension(mpWidth/4, mpHeight/2); // 1/4 panel dimensions
	
	int ox=round(0.5 * dim1.width), oy=round(0.8 * dim1.height); // frame origin coordinates
	
//	Dimension dimDF; // Data Field dimension

	/* 
	 * Graph 
	 */
	double resize_frac=1;
	double radaxis=0.1*Math.min(dim1.width, dim1.height);
	double zaxis=666*1.0; // multiple of 37 (111 = 37 x 3)
	double radaxis_ud=0.0; // updated radial axis length
	
	/* 
	 * Single data 
	 */
	int np=0; // number of variables/parameters -> nof radial axes
	int nt=0; // number of test samples -> number of plots at z axis level
	double ax=-60, ap=0; // angles of rotation around each axis
	int start_hue=100, end_hue=360; // start and end hue values
	int pli=0; // parameter line index for tracing paramter value change for each test
	int hue_steps=0;
	String text="";
	
	/* 
	 * Arrays
	 */
	double[][] base_radial;
	double[][] base_radials_def;
	double[] paxis; // axis perpendicular to plane of radar graph
	double[] paxis_def;
	double[][][] data_radial;
	double[][][] data_radial_def;
	double[][] radial_center;
	double[][] radial_center_def;
	double[][] param_radial;
	double[][] param_radial_def;
	
	boolean[] display_data; // displays test radial if true, otherwise no display
	
	int[] rgb=new int[3];
	int[][] quad=new int[3][4];
	int[][] plane=new int[3][np]; // plane of a test parameter
	
	String[] head;
	double[] param_max;
	ArrayList<double[]> data=new ArrayList<>();
	ArrayList<double[]> ndata=new ArrayList<>();
	
	JButton[] data_button; // button for each test polygon displayed on graph
	JButton[] param_button;
	JTextField[] data_field;
	
	/* 
	 * Color
	 */
	Color c1=null, c2=null; // Drawing polygon, Filling polygon
	
	Color base_radial_pz=Color.black; // base radial color for +ve z
	Color base_radial_nz=Color.gray; // base radial color for -ve z
	
	Graphics2D graph;

	Border outBorder, inBorder;
	
	/*
	 * Image Icons
	 */
	String cwd=System.getProperty("user.dir");
	ImageIcon data_icon1=new ImageIcon(cwd+"/Images/data_icon1.png"); // hover button 1 to view parameter values
	ImageIcon data_icon2=new ImageIcon(cwd+"/Images/data_icon2.png"); // hover button 2 to view parameter values
	ImageIcon param_icon=new ImageIcon(cwd+"/Images/param_icon.png"); // hover button to view highlight parameter
	int diWidth=data_icon1.getIconWidth();
	int diHeight=data_icon1.getIconHeight();
	
	/* 
	 * Class objects
	 */
	Vector_Functions vf=new Vector_Functions();
	Utility_Functions uf=new Utility_Functions();
	
	// -----------------------------------------------------------------------------------
	// Functions -------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------

	int round(double r) { return((int)Math.round(r)); } // round function
	
	/*
	 * Setting Graphing panel
	 */
	void setGraphPanel()
	{
		setBackground(Color.black);
		setBorder(BorderFactory.createLineBorder(Color.white, 1, true));
		setPreferredSize(dim1);
		setLayout(null);
		addMouseMotionListener(this);
		addMouseListener(this);
		addKeyListener(this);
	}
	
	/*
	 * Setting Data panel to display paramater values of a particular test
	 */
	void setDataPanel()
	{
		data_panel.setBorder(BorderFactory.createLineBorder(Color.white, 1));
		// data_panel.setLayout(new GridLayout(2, 1, 10, 10));
		data_panel.setLayout(new GridLayout(1, 1, 10, 10));
		data_panel.addMouseListener(this);
		
		data_value_panel.setPreferredSize(dim2);		
		data_value_panel.setLayout(new GridLayout(np, 1, 0, 0));
		
		data_field=new JTextField[np];
		
		Dimension dimDF=new Dimension(dim2.width, round(dim2.height / np)); // Data Field dimension
		
		// text field features
		for(int c=0; c<np; c++)
		{
			data_field[c]=new JTextField();
			data_field[c].setPreferredSize(dimDF);
			data_field[c].setForeground(Color.cyan);
			data_field[c].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
			data_field[c].setBackground(Color.black); 
			data_field[c].setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 1));
			data_field[c].setCaretColor(Color.cyan);
			data_field[c].setEditable(false);
			
			data_value_panel.add(data_field[c]);
		}
		
		// text field data
		data_panel.add(data_value_panel);
		set_text_data(-1);
	}

	void set_text_data(int r)
	{
		String dash1="                               ";
		if(r==-1)
		{
			for(int c=0; c<np; c++)
			{
				text=Integer.toString(c+1) + " : ";
				text+=head[c] + " ";
				text=text + dash1.substring(0, dash1.length() - text.length() + 1);
				text+=":    0";
				data_field[c].setText(text);
			}
			return;
		}

		for(int c=0; c<np; c++)
		{
			text=Integer.toString(c+1) + " : ";
			text+=head[c] + " "; 
			text+=dash1.substring(0, dash1.length() - text.length());
			text+=":    ";
			text+=Double.toString(data.get(r)[c]);
			text+="   (MAX - " + Double.toString(param_max[c]) + ")";
			data_field[c].setText(text);
		}
	}
	
	void setMainPanel()
	{
		main_panel.setPreferredSize(dimMP);
		
		main_panel.setBackground(Color.white);
		
		main_panel.setLayout(new GridLayout(1, 2, 0, 0));
		main_panel.add(this);
		main_panel.add(data_panel);
	}
	
	void setFrame()
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		
		setGraphPanel();
		setDataPanel();
		setMainPanel();
		
		frame.add(main_panel);
		frame.pack();
		
		cframeWidth=frame.getContentPane().getWidth();
		cframeHeight=frame.getContentPane().getHeight();
		
        frame.setLocationRelativeTo(null);
        frame.setTitle("3D Radar Graph");
        frame.setVisible(true);
	}
	
	/**
	 * Initializing graph data, default data and test data buttons 
	 * @param nt - number of tests
	 * @param np - number of parameters in each test
	 * @param gdp - graph data points
	 * @param rdc - centers of each radial data of a test
	 */
	void initialize(int nt, int np, double[][][] gdp, double[][] rdc)
	{   
        this.np=np;
        this.nt=nt;
        
        data_radial=new double[nt][np][3];
        data_radial_def=new double[nt][np][3];
        data_radial=gdp;
        
        display_data=new boolean[nt];
        
        radial_center=new double[nt][3];
        radial_center_def=new double[nt][3];
        radial_center=rdc;
        
        base_radial=new double[np][3];
        base_radials_def=new double[np][3];
        paxis=new double[3];
        paxis_def=new double[3];
        
        data_button=new JButton[nt];
		param_button=new JButton[np];
		param_radial=new double[np][3];
		param_radial_def=new double[np][3];
        
        generate_radials();
        initialize_defaults();
		set_data_buttons();
		set_param_button();
	}
	
	/**
	 * Setting head and max
	 * @param head - header row of tabulated data
	 * @param param_max - max value in parameter column of data
	 */
	void set_data(String[] head, ArrayList<double[]> data, double[] param_max)
	{
		this.head=head;
		this.data=data;
		this.param_max=param_max;
	}
	
	/**
	 * Setting buttons for each test
	 */
	void set_data_buttons()
	{
		for(int r=0; r<nt; r++)
		{
			data_button[r]=new JButton();
			data_button[r].setIcon(data_icon1);
			data_button[r].setBackground(null);
			data_button[r].setBorderPainted(false);
			data_button[r].setFocusable(false);
			data_button[r].setSize(data_icon1.getIconWidth(), data_icon1.getIconHeight());
			data_button[r].setLocation(round(ox + radial_center[r][x] + dim1.getWidth()*0.4 - data_icon1.getIconWidth()/2), round(oy - radial_center[r][y] - data_icon1.getIconHeight()/2));
			data_button[r].addMouseListener(this);
			
			add(data_button[r]);
		}
	}
	
	/**
	 * Generating base radials and rating (perpendicular) axis
	 */
	void generate_radials()
	{	
		for(int i=0; i<np; i++)
		{
			base_radial[i][x]=vf.p2cx(radaxis*1.5, i * 2 * pi/np);
			base_radial[i][y]=vf.p2cy(radaxis*1.5, i * 2 * pi/np);

			param_radial[i][x]=vf.p2cx(radaxis*1.7, i * 2 * pi/np);
			param_radial[i][y]=vf.p2cy(radaxis*1.7, i * 2 * pi/np);
		}
		
		paxis[x]=0; paxis[y]=0; paxis[z]=zaxis;
	}

	/**
	 * Setting buttons for each parameter
	 */
	void set_param_button()
	{
		for(int c=0; c<np; c++)
		{
			param_button[c]=new JButton();
			param_button[c].setIcon(param_icon);
			param_button[c].setBorderPainted(false);
			param_button[c].setSize(param_icon.getIconWidth(), param_icon.getIconHeight());
			param_button[c].setLocation(round(ox + param_radial[c][x] - param_icon.getIconWidth()*0.5), round(oy - param_radial[c][y] - param_icon.getIconHeight()*0.5));
			param_button[c].addMouseListener(this);

			add(param_button[c]);
		}
	}
	
	/**
	 * Default axes variables to reset graph
	 */
	void initialize_defaults()
	{
		for(int r=0; r<nt; r++)
		{
			for(int c=0; c<np; c++)
			{
				data_radial_def[r][c][x]=data_radial[r][c][x];
				data_radial_def[r][c][y]=data_radial[r][c][y];
				data_radial_def[r][c][z]=data_radial[r][c][z];
			}
		}
		
		for(int c=0; c<np; c++)
		{
			base_radials_def[c][x]=base_radial[c][x];
			base_radials_def[c][y]=base_radial[c][y];
			base_radials_def[c][z]=base_radial[c][z];
		}
		
		paxis_def[x]=paxis[x];
		paxis_def[y]=paxis[y];
		paxis_def[z]=paxis[z];
		
		for(int r=0; r<nt; r++)
		{
			radial_center_def[r][x]=radial_center[r][x];
			radial_center_def[r][y]=radial_center[r][y];
			radial_center_def[r][z]=radial_center[r][z];
		}
		
		for(int r=0; r<nt; r++)
		{
			display_data[r]=true;
		}

		for(int c=0; c<np; c++)
		{
			param_radial_def[c][x]=param_radial[c][x];
			param_radial_def[c][y]=param_radial[c][y];
			param_radial_def[c][z]=param_radial[c][z];
		}
	}
	
	/**
	 * Assigning default values to axes variables during reset 
	 */
	void assign_def() 
	{
		for(int r=0; r<nt; r++)
		{
			for(int c=0; c<np; c++)
			{
				data_radial[r][c][x]=data_radial_def[r][c][x];
				data_radial[r][c][y]=data_radial_def[r][c][y];
				data_radial[r][c][z]=data_radial_def[r][c][z];
			}
		}
		
		for(int c=0; c<np; c++)
		{
			base_radial[c][x]=base_radials_def[c][x];
			base_radial[c][y]=base_radials_def[c][y];
			base_radial[c][z]=base_radials_def[c][z];
		}
		
		paxis[x]=paxis_def[x];
		paxis[y]=paxis_def[y];
		paxis[z]=paxis_def[z];
		
		for(int r=0; r<nt; r++)
		{
			radial_center[r][x]=radial_center_def[r][x];
			radial_center[r][y]=radial_center_def[r][y];
			radial_center[r][z]=radial_center_def[r][z];
		}

		for(int c=0; c<np; c++)
		{
			param_radial[c][x]=param_radial_def[c][x];
			param_radial[c][y]=param_radial_def[c][y];
			param_radial[c][z]=param_radial_def[c][z];
		}
	}
	
	/**
	 * Resizing radials according to the resizing of the frame 
	 * @param f - fraction increase/decrease in frame
	 */
	void resize_radials(double f)
	{
		for(int i=0; i<np; i++)
		{
			base_radial[i][x]*=f;
			base_radial[i][y]*=f;
			base_radial[i][z]*=f;
			
			base_radials_def[i][x]*=f;
			base_radials_def[i][y]*=f;
			base_radials_def[i][z]*=f;
		}
		
		paxis[x]*=f; paxis[y]*=f; paxis[z]*=f;
		paxis_def[x]*=f; paxis_def[y]*=f; paxis_def[z]*=f;
		
		for(int r=0; r<nt; r++)
		{
			for(int c=0; c<np; c++)
			{
				data_radial[r][c][x]*=f;
				data_radial[r][c][y]*=f;
				data_radial[r][c][z]*=f;
				
				data_radial_def[r][c][x]*=f;
				data_radial_def[r][c][y]*=f;
				data_radial_def[r][c][z]*=f;
			}
		}
		
		for(int r=0; r<nt; r++)
		{
			radial_center[r][x]*=f;
			radial_center[r][y]*=f;
			radial_center[r][z]*=f;
			
			radial_center_def[r][x]*=f;
			radial_center_def[r][y]*=f;
			radial_center_def[r][z]*=f;
		}

		for(int c=0; c<np; c++)
		{
			param_radial[c][x]*=f;
			param_radial[c][y]*=f;
			param_radial[c][z]*=f;

			param_radial_def[c][x]*=f;
			param_radial_def[c][y]*=f;
			param_radial_def[c][z]*=f;
		}
		
		radaxis*=f;
		
		update_data_buttons();
		update_param_buttons();
		
		repaint();
	}
	
	/**
	 * Resizing frame and it's components
	 */
	void resizeFrame()
	{	
		frameWidth=frame.getContentPane().getWidth();
		frameHeight=frame.getContentPane().getHeight();
		
		if(frameWidth != cframeWidth || frameHeight != cframeHeight)
		{	
			radaxis_ud=0.2 * Math.min(getWidth(), getHeight());
			resize_frac=radaxis_ud/radaxis;
			
			resize_radials(resize_frac);
		}
		
		cframeWidth=frameWidth; cframeHeight=frameHeight;
		
		dim1.width=frameWidth/2; dim1.height=frameHeight;
		ox=round(getWidth()* 0.5); // keeping graph at centre
		
		dim1.width=frameWidth/2; dim1.height=frameHeight;
		
		setSize(dim1);
		data_panel.setSize(dim1);
	}
	
	/**
	 * Updating graph points, data button locations
	 */
	void update_graph()
	{
		for(int i=0; i<np; i++)
		{
			if(ax!=0) base_radial[i]=vf.Rx(base_radial[i], ax);
			if(ap!=0) base_radial[i]=vf.Rp(paxis, base_radial[i], ap);
		}
		
		paxis=vf.Rx(paxis, ax);
		
		for(int r=0; r<nt; r++)
		{
			for(int c=0; c<np; c++)
			{
				if(ax!=0) data_radial[r][c]=vf.Rx(data_radial[r][c], ax);
				if(ap!=0) data_radial[r][c]=vf.Rp(paxis, data_radial[r][c], ap);
			}
		}
		
		for(int r=0; r<nt; r++)
		{
			if(ax!=0) radial_center[r]=vf.Rx(radial_center[r], ax);
			if(ap!=0) radial_center[r]=vf.Rp(paxis, radial_center[r], ap);
		}

		for(int i=0; i<np; i++)
		{
			if(ax!=0) param_radial[i]=vf.Rx(param_radial[i], ax);
			if(ap!=0) param_radial[i]=vf.Rp(paxis, param_radial[i], ap);
		}
		
		update_data_buttons();
		update_param_buttons();
		
		repaint();
	}
	
	/**
	 * Updating data button locations
	 */
	void update_data_buttons()
	{
		for(int r=0; r<nt; r++)
		{
			data_button[r].setLocation(round(ox + radial_center[r][x] + dim1.getWidth()*0.4 - diWidth/2), round(oy - radial_center[r][y] - diHeight/2));
		}
	}

	void update_param_buttons()
	{
		for(int c=0; c<np; c++)
		{
			param_button[c].setLocation(round(ox + param_radial[c][x] - param_icon.getIconWidth()*0.5), round(oy - param_radial[c][y] - param_icon.getIconHeight()*0.5));
		}
	}

	void shiftUpDown(int d)
	{
		if(d==1) oy-=30;
		else oy+=30;

		update_data_buttons();
		update_param_buttons();
		repaint();
	}
	
	// -----------------------------------------------------------------------------------
	// Drawing Functions -----------------------------------------------------------------
	// -----------------------------------------------------------------------------------
	
	int x1=0, y1=0, x2=0, y2=0;
	
	/**
	 * Drawing radials on graph
	 * @param radial - radial points in cartesian system
	 * @param c - color of the radial lines
	 */
	void draw_radial(double[][] radial, Color c)
	{
		x2=round(ox); y2=round(oy);
		for(int i=0; i<np; i++)
		{
			x1=round(ox + radial[i][x]);
			y1=round(oy - radial[i][y]);
			
			graph.setStroke(new BasicStroke(0.5f));
			graph.setColor(c);
			
			if(i==0)
			{
				graph.setStroke(new BasicStroke(3.5f));
				graph.setColor(Color.white);
			}
			graph.drawLine(x2, y2, x1, y1);
		}
	}
	
	/**
	 * Drawing radial planes on graph
	 * @param radial - radial points in cartesian system
	 * @param c - color of the radial plane
	 */
	void draw_polygon(double[][] radial, Color c)
	{
		for(int i=0; i<np; i++) // drawing polygon of side np
		{
			x1=round(ox + radial[i][x]);
			y1=round(oy - radial[i][y]);
			
			if(i==np-1)
			{
				x2=round(ox + radial[0][x]);
				y2=round(oy - radial[0][y]);
			}
			else 
			{
				x2=round(ox + radial[i+1][x]);
				y2=round(oy - radial[i+1][y]);
			}
			
			graph.setStroke(new BasicStroke(1));
			graph.setColor(c);
			graph.drawLine(x1, y1, x2, y2);
		}
	}
	
	/**
	 * Color filling radial planes 
	 * @param radial - radial points in cartesian system
	 * @param c - color of radial plane
	 */
	void fill_plane(double[][] radial, Color c)
	{
		plane=uf.get_plane(radial, np, ox, oy);
		graph.setColor(c);
		graph.fillPolygon(plane[x], plane[y], np);
	}

	/**
	 * Tracing change in value of particular parameter for each test
	 * @param c - Column index for line tracing
	 */
	void param_line(int c)
	{
		int x1=0, x2=0, y1=0, y2=0;

		graph.setColor(Color.white);

		for(int r=0; r<nt-1; r++)
		{
			x1=round(ox + data_radial[r][c][x]); 
			y1=round(oy - data_radial[r][c][y]);
				
			x2=round(ox + data_radial[r+1][c][x]); 
			y2=round(oy - data_radial[r+1][c][y]);

			graph.drawLine(x1, y1, x2, y2);
		}
	}
	
	// -----------------------------------------------------------------------------------
	// Paint Function --------------------------------------------------------------------
	// -----------------------------------------------------------------------------------
	
	public void paint(Graphics g)
	{
		graph=(Graphics2D)g;
		super.paint(g);
		
		resizeFrame();
		
		int x1=0, y1=0, x2=0, y2=0;
		
		graph.setStroke(new BasicStroke(0.5f));
		graph.setColor(Color.cyan);
		
		for(int i=0; i<2*dim1.width; i+=5) { graph.drawLine(i, oy, i, oy); } // fixed vartical axis
		
		for(int i=0; i<2*dim1.height; i+=5) { graph.drawLine(ox, i, ox, i); } // fixed horizontal axis
		
		for(int i=0; i<360; i+=2) // radar circle
		{
			x1=round(ox + vf.p2cx(radaxis * 1.5, i*pi/180));
			y1=round(oy - vf.p2cy(radaxis * 1.5, i*pi/180));
			
			graph.drawLine(x1, y1, x1, y1);
		}

		x1=ox; y1=oy; x2=round(ox+paxis[x]); y2=round(oy-paxis[y]);
		
		hue_steps=end_hue - start_hue; // color range
		hue_steps=hue_steps/nt;
		
		// Drawing base radial first, followed by test radials
		if(paxis[z]>0)
		{
			// filling and drawing the base radial plane
			// fill_plane(base_radial, base_radial_pz);
			draw_radial(base_radial, Color.white);
			draw_polygon(base_radial, Color.white);
			
			// drawing perpendicular axis
			graph.setColor(Color.cyan);
			graph.setStroke(new BasicStroke(1));
			graph.drawLine(x1, y1, x2, y2);
			
			// Drawing radial plane centers and extended line to keep track of the corresponding radial plane
			for(int r=0; r<nt; r++)
			{
				x1=round(ox + radial_center[r][x]); y1=round(oy - radial_center[r][y]);
				
				graph.setColor(Color.white);
				graph.setStroke(new BasicStroke(3));
				graph.drawLine(x1, y1, x1, y1);
				
				// connecting radials to their corresponding button
				x1=round(ox + radial_center[r][x]); y1=round(oy - radial_center[r][y]);				
				x2=round(ox + (radial_center[r][x] + dim1.getWidth()*0.4)); y2=round(oy - radial_center[r][y]);
				
				graph.setStroke(new BasicStroke(0.01f));
				
				for(int k=x1; k<=x2; k+=2)
				{
					graph.drawLine(k, y1, k, y2);
				}
			}
			
			// Plotting graph points
			for(int r=0; r<nt; r++)
			{	
				rgb=uf.hsv2rgb(hue_steps*r, 75, 100);
				c1=new Color(rgb[0], rgb[1], rgb[2]);
				c2=new Color(rgb[0], rgb[1], rgb[2], opacity);

				//filling and drawing the test radial plane
				if(display_data[r]==true)
				{
					draw_polygon(data_radial[r], c1); 
					fill_plane(data_radial[r], c2);
				}
			}
		}
		else // drawing test radials first, followed by base radials
		{	
			for(int r=0; r<nt; r++) // plotting graph points
			{
				rgb=uf.hsv2rgb(hue_steps*r, 75, 100);
				
				c1=new Color(rgb[0], rgb[1], rgb[2]); // polygon color
				c2=new Color(rgb[0], rgb[1], rgb[2], 50); // plane color with opacity

				// filling and test radial plane
				if(display_data[r]==true)
				{
					draw_polygon(data_radial[r], c1); 
					fill_plane(data_radial[r], c2);
				}
			}
			
			// drawing perpendicular axis
			graph.setColor(Color.cyan);
			graph.setStroke(new BasicStroke(1));
			graph.drawLine(x1, y1, x2, y2);
			
			// Drawing radial plane centers and extended line to the button to keep track of the corresponding radial plane
			for(int r=0; r<nt; r++)
			{
				x1=round(ox + radial_center[r][x]); y1=round(oy - radial_center[r][y]);
				
				graph.setColor(Color.white);
				graph.setStroke(new BasicStroke(3));
				graph.drawLine(x1, y1, x1, y1);
				
				// connecting radials to their corresponding buttons
				x1=round(ox + radial_center[r][x]); y1=round(oy - radial_center[r][y]);				
				x2=round(ox + radial_center[r][x] + dim1.getWidth()*0.4); y2=round(oy - radial_center[r][y]);
				
				graph.setStroke(new BasicStroke(0.01f));
				
				for(int k=x1; k<=x2; k+=2)
				{
					graph.drawLine(k, y1, k, y2);
				}
			}
			
			// filling and drawing the base radial plane
			fill_plane(base_radial, base_radial_nz);
			draw_radial(base_radial, Color.gray);
		}

		// param_line(pli);
	}
	
	// -----------------------------------------------------------------------------------
	// Override Methods ------------------------------------------------------------------
	// -----------------------------------------------------------------------------------
	
	@Override
	public void keyPressed(KeyEvent ke) 
	{
		if(ke.getKeyCode()==KeyEvent.VK_LEFT) // anti clockwise rotation
		{	
			ax=0; ap=da;
			update_graph();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_RIGHT) // clockwise rotation
		{
			ax=0; ap=-da;
			update_graph();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_UP) // upward rotation around x
		{	
			ax=-da; ap=0;
			update_graph();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_DOWN) // downward rotation around x
		{
			ax=da; ap=0;
			update_graph();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_Q) // Quit frame
		{
			frame.dispose();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_R) // reset graph
		{	
			ax=-60; ap=0;
			assign_def();
			
			ox=round(0.5 * dim1.width); oy=round(0.77 * dim1.height); 

			update_graph();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_C)
		{
			ax=0; ap=0;
			assign_def();
			
			ox=round(0.5 * dim1.width); oy=round(0.5 * dim1.height); 

			update_graph();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_EQUALS || ke.getKeyCode()==KeyEvent.VK_P)
		{
			resize_radials(1.05);
		}
		else if(ke.getKeyCode()==KeyEvent.VK_MINUS || ke.getKeyCode()==KeyEvent.VK_N)
		{
			resize_radials(0.95);
		}
		else if(ke.getKeyCode()==KeyEvent.VK_H)
		{
			for(int r=0; r<nt; r++)
			{
				display_data[r]=false;
			}
			repaint();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_S)
		{
			for(int r=0; r<nt; r++)
			{
				display_data[r]=true;
			}
			repaint();
		}
		else if(ke.getKeyCode()==KeyEvent.VK_U)
		{
			shiftUpDown(1);
		}
		else if(ke.getKeyCode()==KeyEvent.VK_D)
		{
			shiftUpDown(-1);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent ke) {}

	@Override
	public void keyReleased(KeyEvent ke) {}
	
	@Override
	public void mouseDragged(MouseEvent me) 
	{
		if(me.getSource()==this)
		{
			oy=me.getY(); // moving graph up and down using mouse
			update_data_buttons();
			update_param_buttons();
			repaint();
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent me) {}

	@Override
	public void mouseClicked(MouseEvent me) 
	{
		for(int r=0; r<nt; r++)
		{
			if(me.getSource()==data_button[r])
			{
				if(display_data[r]==false) display_data[r]=true;
				else display_data[r]=false;
			}
		}

		repaint();
	}

	@Override
	public void mousePressed(MouseEvent me) {}

	@Override
	public void mouseReleased(MouseEvent me) {}

	@Override
	public void mouseEntered(MouseEvent me) 
	{	
		if(me.getSource()==this)
		{
			requestFocus();
		}
		for(int r=0; r<nt; r++)
		{
			if(me.getSource()==data_button[r])
			{
				data_button[r].setIcon(data_icon2);
				data_button[r].setSize(data_icon2.getIconWidth(), data_icon2.getIconHeight());
				data_button[r].setLocation(data_button[r].getX(), data_button[r].getY());
				set_text_data(r);
			}
		}
		for(int c=0; c<np; c++)
		{
			if(me.getSource()==param_button[c])
			{
				data_field[c].setForeground(Color.yellow);

				outBorder=BorderFactory.createMatteBorder(0, 10, 0, 0, Color.yellow);
				inBorder=BorderFactory.createLineBorder(Color.YELLOW, 1, true);

				data_field[c].setBorder(BorderFactory.createCompoundBorder(outBorder, inBorder));

				pli=c;

				repaint();
			}
		}
	}

	@Override
	public void mouseExited(MouseEvent me) 
	{
		for(int r=0; r<nt; r++)
		{
			if(me.getSource()==data_button[r])
			{
				data_button[r].setIcon(data_icon1);
				data_button[r].setSize(data_icon1.getIconWidth(), data_icon1.getIconHeight());
				data_button[r].setLocation(data_button[r].getX(), data_button[r].getY());
			}
		}

		for(int c=0; c<np; c++)
		{
			if(me.getSource()==param_button[c])
			{
				data_field[c].setForeground(Color.cyan);
				data_field[c].setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 1));
			}
		}
	}
}
