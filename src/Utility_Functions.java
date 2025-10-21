import java.util.ArrayList;

public class Utility_Functions
{
	final int x=0, y=1, z=2;
	final int p=20, g=21, c=22;
	
	int round(double r) { return((int)Math.round(r)); } // round function
	
	/**
	 * HSV to RGB converter
	 * @param h - hue
	 * @param s - saturation
	 * @param v - value
	 * @return - (r, g, b) color code of the corresponding (h, s, v)
	 */
	int[] hsv2rgb(double h, double s, double v)
	{
		s=s/100; v=v/100;
		double c, x, m;
		c=s*v;
		x=c*(1 - Math.abs((h/60)%2 - 1));
		m=v-c;
		
		double rd=0, gd=0, bd=0;
		if(h>=0 && h<60)
		{
			rd=c; gd=x; bd=0;
		}
		else if(h>=60 && h<120)
		{
			rd=x; gd=c; bd=0;
		}
		else if(h>=120 && h<180)
		{
			rd=0; gd=c; bd=x;
		}
		else if(h>=180 && h<240)
		{
			rd=0; gd=x; bd=c;
		}
		if(h>=240 && h<300)
		{
			rd=x; gd=0; bd=c;
		}
		else if(h>=300 && h<360)
		{
			rd=c; gd=0; bd=x;
		}
		
		int r=round((rd+m)*255);
		int g=round((gd+m)*255);
		int b=round((bd+m)*255);
		
		return(new int[] {r, g, b});
	}
	
	/**
	 * Getting quadrilateral bounded by the 4 points (3 x 4 matrix)
	 * @param p0 - cartesian point 0
	 * @param p1 - cartesian point 0
	 * @param p2 - cartesian point 0
	 * @param p3 - cartesian point 0
	 * @param ox - origin x
	 * @param oy - origin y
	 * @return - quadrilateral array points bounded by the 4 points 
	 */
	int[][] get_quad(double[] p0, double[] p1, double[] p2, double[] p3, int ox, int oy) // get transpose of coordinates for color filling polygon
	{
		int[][] quad=new int[3][4];
		
		quad[x][0]=round(ox + p0[x]);
		quad[x][1]=round(ox + p1[x]);
		quad[x][2]=round(ox + p2[x]);
		quad[x][3]=round(ox + p3[x]);
		
		quad[y][0]=round(oy - p0[y]);
		quad[y][1]=round(oy - p1[y]);
		quad[y][2]=round(oy - p2[y]);
		quad[y][3]=round(oy - p3[y]);
		
		quad[z][0]=round(p0[z]);
		quad[z][1]=round(p1[z]);
		quad[z][2]=round(p2[z]);
		quad[z][3]=round(p3[z]);
		
		return(quad);
	}
	
	/**
	 * Getting plane bounded by the points of a test (3 x np matrix) 
	 * @param p - test points
	 * @param np - nof parameters
	 * @param ox - origin x
	 * @param oy - origin y
	 * @return - plane poiunts
	 */
	int[][] get_plane(double[][] p, int np, int ox, int oy)
	{
		int[][] plane=new int[3][np];
		
		for(int c=0; c<np; c++)
		{
			plane[x][c]=round(ox + p[c][x]);
			plane[y][c]=round(oy - p[c][y]);
			plane[z][c]=round(ox + p[c][z]);
		}
		
		return(plane);
	}

	// rounding function to n decimal places
	double round2n(double d, double n) { return(Math.round(d * Math.pow(10, n)) / Math.pow(10, n)); }

	void display_data(ArrayList<double[]> d)
	{
		for(int r=0; r<d.size(); r++)
		{
			for(int c=0; c<d.get(r).length; c++)
			{
				System.out.println(d.get(r)[c]);
			}
			System.out.println();
		}
	}

	/**
	 * Processsing raw data from CSV file
	 * @param rawdata - all data containing parameters and ratings from testing
	 * Column indices written in UsageDirections.txt
	 */
	ArrayList<double[]> filter_data1(ArrayList<String[]> rawdata, String[] head, int[] inp, int op)
	{
		ArrayList<String[]> data1=new ArrayList<>();
		for(String[] rd: rawdata)
		{
			if(rd.length>=op)
			{
				if(rd[op].compareTo("")!=0) data1.add(rd);
			}
		}
		
		int nt=data1.size() - 1; // number of rated tests
		
		int np=inp.length;

		for(int c=0, k=0; c<np; c++)
		{
			head[c]=data1.get(0)[inp[k]]; ++k;
		}

		data1.remove(0);

		ArrayList<double[]> data2=new ArrayList<>();

		// converting String to double data type and finding max value in each parameter
		for(int r=0; r<nt; r++) // iterating through tests
		{
			double[] d=new double[np+1];
			for(int c=0, k=0; c<np; c++) // iterating through parameters
			{
				d[c]=Double.valueOf(data1.get(r)[inp[k]]); ++k;
			}

			d[np]=Double.valueOf(data1.get(r)[op]);

			data2.add(d);
		}
		return(data2);
	}
}
