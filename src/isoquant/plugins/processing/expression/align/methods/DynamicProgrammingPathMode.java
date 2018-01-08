package isoquant.plugins.processing.expression.align.methods;

import de.mz.jk.ms.align.com.DTWPathDescription;

public enum DynamicProgrammingPathMode
{
	LEFT, RIGHT, BOTH;

	public static DynamicProgrammingPathMode fromString(String pathDesc)
	{
		String d = pathDesc.toLowerCase();
		if (d.contains( "right" )) return RIGHT;
		if (d.contains( "left" )) return LEFT;
		if (d.contains( "both" )) return BOTH;
		if (d.startsWith( "r" )) return RIGHT;
		if (d.startsWith( "l" )) return LEFT;
		return BOTH;
	}

	public DTWPathDescription[] toDTWPathDescription()
	{
		switch (this)
		{
			case LEFT:
				return new DTWPathDescription[] { DTWPathDescription.LEFT };
			case RIGHT:
				return new DTWPathDescription[] { DTWPathDescription.RIGHT };
			case BOTH:
			default:
				return new DTWPathDescription[] { DTWPathDescription.LEFT, DTWPathDescription.RIGHT };
		}
	}
}