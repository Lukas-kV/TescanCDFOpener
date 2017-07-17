import java.awt.Label;
import java.awt.Panel;
import javax.swing.BoxLayout;

import MetaStruct.MetaStruct;
import java.awt.Component;
import javax.swing.Box;

public class MetaDataViewer extends Panel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Label lblCamera, lblObjective, lblPosition, lblTuning, lblTuning2, lblTimestamp;
    private int ltimeSlot, lrecordIndex;
    private Component horizontalGlue;

    /**
     * Create the frame.
     */
    public MetaDataViewer()
    {
        
        lblCamera = new Label("Camera");
        lblObjective = new Label("Objective");
        lblPosition = new Label("Position");
        lblTuning = new Label("Tuning");
        lblTuning2 = new Label("Tuning2");
        lblTimestamp = new Label("Timestamp");
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        add(lblTimestamp);
        add(lblCamera);
        add(lblObjective);
        add(lblPosition);
        add(lblTuning);
        add(lblTuning2);
        
        horizontalGlue = Box.createHorizontalGlue();
        add(horizontalGlue);
        
        ltimeSlot = -1;
        lrecordIndex = -1;

    }
    
    public void update(MetaData m, int timeSlot, int recordIndex)
    {
        if (ltimeSlot != timeSlot)
        {
            ltimeSlot = timeSlot;
            
            String time = m.getGlobalTime(timeSlot, 0, 0);

            if (time != null)
            {
                String[] d = time.split("T");
                if (d.length > 1)
                    lblTimestamp.setText("Timestamp: " + d[0] + "  " + d[1]);
            }
        }

        if (lrecordIndex != recordIndex)
        {
            lrecordIndex = recordIndex;
            
            double[] pos = m.get3DPositions(recordIndex);
            if (pos != null)
                lblPosition.setText(String.format("Position X: %.6f mm Y: %.6f  Z: %.3f µm", pos[0], pos[1], pos[2]));
            else
                lblPosition.setText("");

            MetaStruct ms = m.getMetaStruct(recordIndex);
            if (ms != null)
            {
                lblCamera.setText(String.format("Exposure: %.0f ms", ms.camera.exposure));

                lblObjective.setText(String.format("Lens: %.0fx/%.2f  FOV: %.2f x %.2f µm", ms.objectives.magnification,
                        ms.objectives.NA, ms.objectives.fov.x, ms.objectives.fov.y));

                lblTuning.setText(String.format("Absolute Z: %.5f mm  Relative Z: %.5f mm", ms.objectives.AbsZ,
                        ms.objectives.RelZ));

                lblTuning2.setText(String.format("Piezo X: %.0f nm  Piezo Y: %.0f nm  Branch Length: %.0f nm",
                        ms.objectives.PieX, ms.objectives.PieY, ms.objectives.branchLength));
            }
            // System.out.println(time);
        }

    }

}
