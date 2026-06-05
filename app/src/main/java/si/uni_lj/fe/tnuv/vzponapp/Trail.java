package si.uni_lj.fe.tnuv.vzponapp;

public class Trail {

    public String title;
    public String description;
    public String gpxPath;
    public String difficulty;
    public float maxEle;
    public float minEle;
    public float distance;
    public boolean longRoute;

    public Trail(
            String title,
            String description,
            String gpxPath,
            String difficulty,
            float maxEle,
            float minEle,
            float distance,
            boolean longRoute
    ) {
        this.title = title;
        this.description = description;
        this.gpxPath = gpxPath;
        this.difficulty = difficulty;
        this.maxEle = maxEle;
        this.minEle = minEle;
        this.distance = distance;
        this.longRoute = longRoute;
    }
}