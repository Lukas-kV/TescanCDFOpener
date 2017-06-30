package MetaStruct;

import com.google.gson.annotations.SerializedName;

public class MetaStruct
{
    public String encoding;
    public String metaVersion;

    public Camera camera;
    public FFT fft;
    
    @SerializedName("fluo_clipping")
    public FluoClipping fluoClipping;
    
    public Illuminator illuminator;
    public Objectives objectives;
    
    @SerializedName("phase_compensation")
    public PhaseCompensating phaseCompensating;
    
    @SerializedName("phase_unwrapping")
    public PhaseUnwrapping phaseUnwrapping;
    
    public Statistics statistics;
    public Temperature temperature;                
}
