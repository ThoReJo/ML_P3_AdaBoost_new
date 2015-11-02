package spamclassificatie;
import java.util.Scanner;
import java.util.Locale;

public class Mail 
{
    public double[] x;
    public int y;
    public double weight;

    public void fromString(String text)
    {
    	x = new double[57];

		Scanner scanner = new Scanner(text);
		scanner.useDelimiter(",");
		scanner.useLocale(Locale.US);
		for (int i = 0; i < x.length; ++i) 
		{
			x[i] = scanner.nextDouble();
		}
		y = scanner.nextInt();
		scanner.close();
    }
    
    public String toString()
    {
		String text = "x = (" + x[0];
		for (int i = 1; i < x.length; ++i) 
		{
		    text += "; " + x[i];
		}
		text += "); y = " + y;
		return text;
    }
}
