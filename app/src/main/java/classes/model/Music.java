package classes.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Music implements Serializable {

    private String title = "";
    private String artist = "";
    private int duration = 0;
    private String path = "";

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Music) {
            Music music = (Music) obj;
            return getPath().equals(music.getPath());
        }
        return super.equals(obj);
    }
}
