package functions.cache;

public class UIContent {
    private String aggregatedFrequencies;
    private String imageURL;

    public UIContent() {}
    public UIContent(String imageURL, String aggregatedFrequencies) {
        this.imageURL = imageURL;
        this.aggregatedFrequencies = aggregatedFrequencies;
    }

    public String getAggregatedFrequencies() {
        return aggregatedFrequencies;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setAggregatedFrequencies(String aggregatedFrequencies) {
        this.aggregatedFrequencies = aggregatedFrequencies;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
