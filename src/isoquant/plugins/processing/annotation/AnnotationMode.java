package isoquant.plugins.processing.annotation;

public enum AnnotationMode
{
	all, unique, razor;
	public static AnnotationMode fromString(String mode)
	{
		mode = mode.trim().toLowerCase();
		return (mode.startsWith("uni")) ? unique : (mode.startsWith("raz")) ? razor : all;
	}
}
