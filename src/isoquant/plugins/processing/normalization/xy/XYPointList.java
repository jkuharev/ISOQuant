package isoquant.plugins.processing.normalization.xy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XYPointList 
{
	private double minX = 0;
	private double maxX = 0;
	public List<XYPoint> points = null;
	
	public XYPointList() 
	{
		points = new ArrayList<XYPoint>();
	}
	
	/**
	 * @return the points
	 */
	public List<XYPoint> getPoints()
	{
		return points;
	}

	public double[] getXArray()
	{
		return getXArray(false);
	}
	
	public double[] getXArray(boolean strictlyIncreasing)
	{
		int n = points.size();
		double[] _x = new double[n];
		double oldX = 0.0;
		
		for(int i=0; i<n; i++)
		{
			_x[i] = points.get(i).x;
			if(strictlyIncreasing)
			{
				if(i>0 && _x[i]<=oldX) _x[i]= oldX + .000001;
				oldX = _x[i];
			}
		}
		
		return _x;
	}
	
	public double[] getYArray()
	{
		int n = points.size();
		double[] _y = new double[n];
		for(int i=0; i<n; i++)
		{
			_y[i] = points.get(i).y;
		}
		return _y;
	}
	
	public void addPoint(double x, double y)
	{
		points.add(new XYPoint(x,y));
		checkMinMax(x);
	}
	
	public void addPoint(int index, double x, double y)
	{
		points.add(index, new XYPoint(x,y));
		checkMinMax(x);
	}
	
	private void checkMinMax(double x) 
	{
		if(points.size()==1)
			minX = maxX = x;
		else if(x<minX)
			minX = x;
		else if(x>maxX)
			maxX = x;			
	}

	public void autoEnlargeBounds()
	{
		int n = points.size();
		if(n<1) return;
		double halfSpanX = (maxX - minX) / 2;
		int tenthN = (int) Math.floor(n/10.0);
		if(tenthN<3) // if we have view points use mean of y 
		{
			double avgY = 0;
			for(int i=0;i<n;i++) avgY += points.get(i).y / n;
			addPoint( 0, minX - halfSpanX, avgY );
			addPoint( maxX + halfSpanX, avgY );
			return;
		}
		
		double[] left = new double[tenthN];
		double[] right = new double[tenthN];
		for(int i=0; i<tenthN; i++)
		{
			left[i] = points.get(i).y;
			right[i] = points.get(n-i-1).y;
		}
		
		addPoint( 0, minX - halfSpanX, median(left) );
		addPoint( maxX + halfSpanX, median(right) );	
	}
	
	public static double median(double[] numbers)
	{
		int n = numbers.length - 1;
		if(n==0) return numbers[0];
		if(n< 0) return 0.0;
		int nh = (int)Math.floor( n / 2.0 );
		Arrays.sort(numbers);
		return (numbers[nh] + numbers[n-nh]) / 2;
	}
}
