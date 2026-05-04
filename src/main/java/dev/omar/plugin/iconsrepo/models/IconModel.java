package dev.omar.plugin.iconsrepo.models;

import com.caverock.androidsvg.SVG;
import java.util.Arrays;
import java.util.Objects;

public class IconModel {
    private String iconName;
    private SVG svgIcon;

    public IconModel(String iconName, SVG svgIcon) {
        this.iconName = iconName;
        this.svgIcon = svgIcon;
    }

    public String getIconName() {
        return this.iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (iconName != null ? iconName.hashCode() : 0);
        result = 31 * result + svgIcon.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        IconModel other = (IconModel) obj;

        if (!Objects.equals(iconName, other.iconName))
            return false;

        return Objects.equals(svgIcon,other.svgIcon);
    }

    public SVG getSvgIcon() {
        return this.svgIcon;
    }

    public void setSvgIcon(SVG svgIcon) {
        this.svgIcon = svgIcon;
    }
}
