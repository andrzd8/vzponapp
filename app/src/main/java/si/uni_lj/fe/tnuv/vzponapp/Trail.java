package si.uni_lj.fe.tnuv.vzponapp;

public class Trail {

    String title;
    String description;
    String distance;
    String weather;
    String difficulty;
    int gpxFile;

    public Trail(
            String title,
            String description,
            String distance,
            String weather,
            String difficulty,
            int gpxFile
    ) {
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.weather = weather;
        this.difficulty = difficulty;
        this.gpxFile = gpxFile;
    }
}