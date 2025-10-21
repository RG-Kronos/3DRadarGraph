public class Vector_Functions
{
	final int x=0, y=1, z=2;
	final double pi=Math.PI;
	double[] av=new double[3];
	
	/**
	 * @param r - radial length
	 * @param a - angle of radial from x axis
	 * @return - array returning cartesian coordinates
	 */
	public double p2cx(double r, double a) { return(r * Math.cos(a)); } // polar to cartesian x coordinate wrt origin
	public double p2cy(double r, double a) { return(r * Math.sin(a)); } // polar to cartesian y coordinate wrt origin
	
	/**
	 * @param v1 - vector 1
	 * @param v2 - vector 2
	 * @return - vectorially added vector
	 */
	public double[] vec_add(double[] v1, double[] v2)
	{
		double[] r= {v1[x] + v2[x], v1[y] + v2[y], v1[z] + v2[z]};
		return(r);
	}
	
	/**
	 * @param v1 - vector 1
	 * @param v2 - vector 2
	 * @return - dot product of vectors (scalar qty)
	 */
	public double dot(double[] v1, double[] v2)
	{
		double r=v1[x]*v2[x] + v1[y]*v2[y] + v1[z]*v2[z];
		return(r);
	}
	
	/**
	 * @param v1 - vector 1
	 * @param v2 - vector 2
	 * @return - cross product of vectors (vector qty)
	 */
	public double[] cross(double[] v1, double[] v2)
	{
		double[] r={v1[y]*v2[z]- v1[z]*v2[y], v1[z]*v2[x] - v1[x]*v2[z], v1[x]*v2[y] - v1[y]*v2[x]};
		return(r);
	}
	
	/**
	 * @param v - vector
	 * @param s - scalar
	 * @return - scalar multiplied vector
	 */
	public double[] scalarprod(double[] v, double s)
	{
		double[] r={v[x]*s, v[y]*s, v[z]*s};
		return(r);
	}
	
	/**
	 * @param paxis - rating (perpendicular) axis vector
	 * @param rv - vector to be rotated around paxis
	 * @param a - angle of rotation
	 * @return - rotated vector
	 */
	public double[] Rp(double[] paxis, double[] rv, double a)
	{	
		a*=pi/180;
		double mod=Math.sqrt(paxis[x]*paxis[x] + paxis[y]*paxis[y] + paxis[z]*paxis[z]);
		av[x]=paxis[x]/mod; av[y]=paxis[y]/mod; av[z]=paxis[z]/mod;
		
		double[] v1=cross(av, rv);
		double[] v2=cross(av, v1);
		
		v1=scalarprod(v1, Math.sin(a));
		v2=scalarprod(v2, 1 - Math.cos(a));
		
		rv=vec_add(rv, v1); rv=vec_add(rv, v2);
		
		return(rv);
	}
	
	/**
	 * @param v - vector to be rotated around x axis
	 * @param a - angle of rotation
	 * @return - rotated vector
	 */
	public double[] Rx(double[] v, double a)
	{
		a*=pi/180;
		double[] r={v[x], v[y]*Math.cos(a) - v[z]*Math.sin(a), v[y]*Math.sin(a) + v[z]*Math.cos(a)};
		return(r);
	}
	
	/**
	 * @param v - vector to be rotated around y axis
	 * @param a - angle of rotation
	 * @return - rotated vector
	 */
	public double[] Ry(double[] v, double a)
	{
		a*=pi/180;
		double[] r={v[x]*Math.cos(a) + v[z]*Math.sin(a), v[y], v[z]*Math.cos(a) - v[x]*Math.sin(a)};
		return(r);
	}
	
	/**
	 * @param v - vector to be rotated around z axis
	 * @param a - angle of rotation
	 * @return - rotated vector
	 */
	public double[] Rz(double[] v, double a)
	{
		a*=pi/180;
		double[] r={v[x]*Math.cos(a) - v[y]*Math.sin(a), v[x]*Math.sin(a) + v[y]*Math.cos(a), v[z]};
		return(r);
	}

}
