package MetaStruct;

public class PhaseCompensating
{
    public PhaseCompensating()
    {
        dynamicMode = false;
        thresholdType = 0;
        polynomial = iterations = 1;
        threshold = thresholdManu = 0.;
    }
    
    public boolean dynamicMode;
    public int iterations, polynomial;
    public double threshold, thresholdManu;
    public int thresholdType;
           
}
