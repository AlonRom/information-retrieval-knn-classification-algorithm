package Lucene;



public class Neighbor implements Comparable<Neighbor> {

    public Integer id;
    public Float distance;

    public Neighbor(Integer id, Float distnace){
        this.id = id;
        this.distance = distnace;
    }

    @Override
    public int compareTo(Neighbor o) {
        return this.distance.compareTo(o.distance);
    }

    public void putIdAndDistnace(Integer id, Float distance){
        this.id = id;
        this.distance = distance;
    }



}
