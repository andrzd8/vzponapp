package si.uni_lj.fe.tnuv.vzponapp;

public class Trail {

    String title;
    String description;

    String distance;
    String difficulty;
    int gpxFile;
    boolean longRoute;
    double maxEle;

    public Trail(
            String title,
            String description,
            String distance,
            String difficulty,
            int gpxFile,
            boolean longRoute
    ) {
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.difficulty = difficulty;
        this.gpxFile = gpxFile;
        this.longRoute = longRoute;
        this.maxEle = 0;
    }
}