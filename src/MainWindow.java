import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.util.ArrayList;

// public class MainWindow extends GraphWindow
public class MainWindow extends GraphWindow
{
	private static final long serialVersionUID = -4959710382085834488L;
	
	final int x=0, y=1, z=2;
	
	Vector_functions vf=new Vector_functions();
	
	double[][][] gdp; // graph data points
	double[][] rdc; // radial data center
	
	// rounding function to n decimal places
	double round2n(double d, double n) { return(Math.round(d * Math.pow(10, n)) / Math.pow(10, n)); }

	/**
	 * Rating algorithm
	 * @param a - percolation rating
	 * @param b - gradient rating
	 * @param c - cracks rating
	 * @return - single rating value
	 */
	int rate(double a, double b, double c) { return(round(a*100 + b*10 + c)); }
	
	/**
	 * Converts radar data into cartesian coordinates for plotting and graphing 
	 * Stores center point of radial data for each test 
	 * @param data - values of parameters for each test
	 * @param nt - number of tests
	 * @param np - number of parameters in each test
	 */
	void generate_graph_data(ArrayList<double[]> data, int nt, int np) // generating graph data points
	{
		gdp=new double[nt][np][3];
		rdc=new double[nt][3];

		double rating=0;
		// int rating_max=rate(10, 10, 10); // SpraySystemsCo Atomizer max rating
		int rating_max=50; // Graco Atomizer max rating
		
		for(int r=0; r<data.size(); r++)
		{
			rating=data.get(r)[np];

			for(int c=0; c<np; c++)
			{	
				// graph data points
				gdp[r][c][x]=vf.p2cx(data.get(r)[c] * radaxis * 1.25, c * 2 * pi/np);
				gdp[r][c][y]=vf.p2cy(data.get(r)[c] * radaxis * 1.25, c * 2 * pi/np);
				// gdp[r][c][z]=round(rating * zaxis / rating_max); // radar data placed on axis proportional to their rating 
				gdp[r][c][z]=round(r * zaxis / data.size()); // radar data placed equidistant on axis
			}

			// radial center points corresponding to the radar data 
			rdc[r][x]=0;
			rdc[r][y]=0;
			// rdc[r][z]=round(rating * zaxis / rating_max);
			rdc[r][z]=round(r * zaxis / data.size());
		}
	}

	ArrayList<double[]> normalize(ArrayList<double[]> d, double[] param_max)
	{
		// finding max value in each parameter
		for(int r=0; r<d.size(); r++)
		{
			for(int c=0; c<param_max.length; c++)
			{
				param_max[c]=Math.max(param_max[c], d.get(r)[c]);
			}
		}

		// normalizing
		for(int r=0; r<d.size(); r++)
		{
			for(int c=0; c<param_max.length; c++)
			{
				d.get(r)[c]/=param_max[c];

				d.get(r)[c]=round2n(d.get(r)[c], 5);
			}
		}

		return(d);
	}

	void export_data(ArrayList<double[]> data, String[] head, String path)
	{
		try 
		{
			FileWriter file=new FileWriter(path);
			BufferedWriter bw=new BufferedWriter(file);

			for(int r=0; r<head.length; r++)
			{
				bw.write(head[r] + ",");
			}
			bw.write("RATING");
			bw.newLine();

			for(int r=0; r<data.size(); r++)
			{
				for(int c=0; c<data.get(r).length; c++)
				{
					if(c==data.get(r).length-1)
					{
						bw.write(Double.toString(data.get(r)[c]));
					}
					else
					{
						bw.write(Double.toString(data.get(r)[c]) + ",");
					}
				}
				bw.newLine();
			}

			bw.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	void display_data(ArrayList<double[]> data)
	{
		for(int i=0; i<data.size(); i++)
		{
			for(int j=0; j<data.get(i).length; j++)
			{
				System.out.print(data.get(i)[j] + "   ,   ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args)  
	{	
		String inputpath="";
		int op=0;
		int[] inp=null;
		String exportpath1="", exportpath2="";

		Scanner in=new Scanner(System.in);
		System.out.println("System INDEX : \n\t(1) SpraySystemsCo  \n\t(2) Graco \n\t(3) Aarna");
		int idx=in.nextInt();

		// System.out.print("Enter CSV Data File Path : "); inputpath=in.nextLine(); 
		// System.out.println();

		// System.out.print("Enter Input Data Column Indexes separated by spaces : ");
		// String st=in.nextLine();
		// ArrayList<Integer> columns=new ArrayList<>(); boolean hasinput=true;
		// st.trim();

		// for(String s: st.split(" "))
		// {
		// 	columns.add(Integer.valueOf(s));
		// }

		// int[] inp=new int[columns.size()];
		// for(int i=0; i<columns.size(); i++)
		// {
		// 	inp[i]=columns.get(i);
		// }

		// System.out.print("\nEnter Output Data Column Index : "); op=in.nextInt();
		// System.out.println();

		if(idx==1)
		{
			inputpath="/Users/rohangupta/Desktop/Files/AtomizerData/SSC.csv";
			inp=new int[]{7, 8, 9, 10, 11, 12, 13, 15, 19}; op=24;

			exportpath1="/Users/rohangupta/Desktop/Files/MLR_Data/InputData_SSC_FV.csv"; // face value data
			exportpath2="/Users/rohangupta/Desktop/Files/MLR_Data/InputData_SSC_NV.csv"; // normalised data
		}
		else if(idx==2)
		{
			inputpath="/Users/rohangupta/Desktop/Files/AtomizerData/Graco.csv";
			inp=new int[] {5, 6, 7, 8, 9, 10, 11, 12}; op=17;

			exportpath1="/Users/rohangupta/Desktop/Files/MLR_Data/InputData_GRC_FV.csv"; // face value data
			exportpath2="/Users/rohangupta/Desktop/Files/MLR_Data/InputData_GRC_NV.csv"; // normalised data
		}
		else if(idx==3)
		{
			inputpath="/Users/rohangupta/Desktop/Files/AtomizerData/Aarna.csv";
			// inp=new int[] {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 34, 35, 36}; op=25;
			inp=new int[] {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}; op=25;

			exportpath1="/Users/rohangupta/Desktop/Files/MLR_Data/InputData_Aarna_FV.csv"; // face value data
			exportpath2="/Users/rohangupta/Desktop/Files/MLR_Data/InputData_Aarna_NV.csv"; // normalised data

		}
		else
		{
			in.close();
			System.out.println("Incorrect Input");
			return;
		}
		
		ArrayList<String[]> Stringdata=new ArrayList<>(); // data in String format

		for(int i=0; i<inp.length; i++)
		{
			inp[i]-=1;
		}
		--op;

		try 
		{
			FileReader file=new FileReader(inputpath);
			BufferedReader br=new BufferedReader(file);
			
			String line=br.readLine(); 
			
			while(line!=null)
			{
				Stringdata.add(line.split(","));
				line=br.readLine();
			}
			br.close();
			file.close();
		} 
		catch (IOException ie) 
		{
			ie.printStackTrace();
		}

		MainWindow mw=new MainWindow();
		Utility_functions uf=new Utility_functions();

		String[] head=new String[inp.length];

		ArrayList<double[]> data=uf.filter_data1(Stringdata, head, inp, op); // data in double format
		ArrayList<double[]> ndata=new ArrayList<>(); // normalised data
		ArrayList<double[]> ndata100=new ArrayList<>(); // normalised data

		for(int r=0; r<data.size(); r++)
		{
			ndata.add(data.get(r).clone());
		}

		double[] param_max=new double[data.get(0).length - 1];

		ndata=mw.normalize(ndata, param_max);

		for(int r=0; r<ndata.size(); r++)
		{
			ndata100.add(ndata.get(r).clone());
			for(int c=0; c<ndata100.get(r).length - 1; c++) // -1 to exclude rating
			{
				ndata100.get(r)[c]*=100; // converting fraction to percentage
			}
		}

		mw.export_data(data, head, exportpath1);
		mw.export_data(ndata100, head, exportpath2);

		int nt=data.size();
		int np=data.get(0).length-1;

		if(np>2 && nt>0)
		{
			mw.generate_graph_data(ndata, nt, np);
			
			mw.initialize(nt, np, mw.gdp, mw.rdc);
			mw.set_data(head, data, param_max);
			mw.setFrame();
			mw.update_graph();
		}
		in.close();
	}
}
