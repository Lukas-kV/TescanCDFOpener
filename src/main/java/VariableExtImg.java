import java.util.Map;

import uk.ac.bristol.star.cdf.Variable;
import uk.ac.bristol.star.cdf.VariableAttribute;

public class VariableExtImg extends VariableExt 
{
	private boolean clippingPossible;
	private int sx, sy;


	public VariableExtImg(Variable _var, Map<String, VariableAttribute> _attributes) 
	{
		super(_var, _attributes);
		// TODO Auto-generated constructor stub
		
		clippingPossible = false;
		sx = getWidth();  sy = getHeight();		
	}

	
	public VariableExtImg(VariableExt gen)
	{
		super(gen.var, gen.attributes);
		
		clippingPossible = false;
		sx = getWidth();  sy = getHeight();
	}
		
	
	public int getWidth()
	{
		return getDimSizes()[1];
	}
	
	public int getHeight()
	{
		return getDimSizes()[0];
	}
		
	public boolean isClippingPossible()
	{
		return clippingPossible;
	}

	public void setClippingPossible(boolean possible)
	{
		clippingPossible = possible;
	}
		
	public void setClippingDimensions(int width, int height)
	{
		sx = width; sy = height;
	}
	
	public int getClippedWidth() 
	{
		return sx;
	}

	public int getClippedHeigth() 
	{
		return sy;
	}
	
}
