package net.sacredlabyrinth.phaed.dynmap.preciousstones.layers;

import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import net.sacredlabyrinth.phaed.dynmap.preciousstones.DynmapPreciousStones;
import net.sacredlabyrinth.phaed.dynmap.preciousstones.Helper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Fields
{

    private final String MARKER_SET = "preciousstones.fields";
    private final String CONFIG = "layer.fields.";
    private final String LABEL = "Precious Stones Fields";
    private DynmapPreciousStones plugin;
    private boolean stop;
    private int task;
    private boolean enable;
    private int updateSeconds;
    private String label;
    private int layerPriority;
    private boolean hideByDefault;
    private int minZoom;
    boolean use3d;
    FieldStyle default_style;
    private Map<String, FieldStyle> field_styles = new HashMap<String, FieldStyle>();
    private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();
    private Map<String, MarkerIcon> icons = new HashMap<String, MarkerIcon>();
    private MarkerIcon default_icon;
    private MarkerSet markerSet;
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    public Fields()
    {
        plugin = DynmapPreciousStones.getInstance();
        readConfig();

        if (enable)
        {
            initMarkerSet();
            initDefaultIcon();
            initStylesAndIcons();
            scheduleNextUpdate(5);
        }
    }

    private void readConfig()
    {
        FileConfiguration cfg = plugin.getCfg();
        enable = cfg.getBoolean(CONFIG + "enable");
        updateSeconds = Math.max(cfg.getInt(CONFIG + "update-seconds"), 2);
        label = cfg.getString(CONFIG + "label", LABEL);
        layerPriority = cfg.getInt(CONFIG + "layer-priority");
        hideByDefault = cfg.getBoolean(CONFIG + "hide-by-default");
        minZoom = Math.max(cfg.getInt(CONFIG + "min-zoom"), 0);
        use3d = cfg.getBoolean(CONFIG + "use3d");
        default_style = new FieldStyle(cfg, "defaultstyle");
    }

    private void initStylesAndIcons()
    {
        FileConfiguration cfg = plugin.getCfg();
        SettingsManager sm = plugin.getPreciousStones().getSettingsManager();
        List<LinkedHashMap<String, Object>> lBlocks = sm.getForceFieldBlocks();
        field_styles.clear();
        icons.clear();
        for (LinkedHashMap<String, Object> iType : lBlocks)
        {
            String sType = iType.get("block") + "_style";
            FieldStyle fs = new FieldStyle(cfg, sType, default_style);
            field_styles.put(sType, fs);
            MarkerIcon this_icon = plugin.getMarkerApi().getMarkerIcon(fs.icon);
            if (this_icon == null)
            {
                this_icon = default_icon;
            }

            icons.put(sType, this_icon);
        }
    }

    private void initDefaultIcon()
    {
        default_icon = plugin.getMarkerApi().getMarkerIcon("preciousstones.default");

        if (default_icon == null)
        {
            InputStream stream = DynmapPreciousStones.class.getResourceAsStream("/images/default.png");
            default_icon = plugin.getMarkerApi().createMarkerIcon("preciousstones.default", "preciousstones.default", stream);
        }

        if (default_icon == null)
        {
            DynmapPreciousStones.severe("Error creating icon");
        }

    }

    private void initMarkerSet()
    {
        markerSet = plugin.getMarkerApi().getMarkerSet(MARKER_SET);

        if (markerSet == null)
        {
            markerSet = plugin.getMarkerApi().createMarkerSet(MARKER_SET, label, null, false);
        }
        else
        {
            markerSet.setMarkerSetLabel(label);
        }

        if (markerSet == null)
        {
            DynmapPreciousStones.severe("Error creating " + LABEL + " marker set");
            return;
        }

        markerSet.setLayerPriority(layerPriority);
        markerSet.setHideByDefault(hideByDefault);
        markerSet.setMinZoom(minZoom);
    }

    private void scheduleNextUpdate(int seconds)
    {
        plugin.getServer().getScheduler().cancelTask(task);
        task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), seconds * 20);
    }

    private class Update implements Runnable
    {

        public void run()
        {
            if (!stop)
            {
                updateMarkerSet();
                scheduleNextUpdate(updateSeconds);
            }
        }
    }

    public void cleanup()
    {
        if (markerSet != null)
        {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }
        resareas.clear();
        stop = true;
    }

    private String getFieldPopup(AreaMarker m, Field f)
    {
        FieldStyle as = field_styles.get(f.getTypeId() + "_style");
        String v = "<div class=\"regioninfo\">" + as.area_text_format + "</div>";
        return replaceSubstitutions(v, f);
    }

    private String replaceSubstitutions(String sInput, Field f)
    {
        String v = sInput;
        String sAllowed = "";
        List<String> allowed = f.getAllowed();
        for (int i = 0; i < allowed.size() - 1; i++)
        {
            String s = allowed.get(i);
            sAllowed += s;
            if (i < allowed.size() - 1)
            {
                sAllowed += ",";
            }
        }
        Location loc = f.getBlock().getLocation();

        v = v.replace("%type%", f.getSettings().getTitle());
        v = v.replace("%name%", f.getName());
        v = v.replace("%owner%", f.getOwner());
        v = v.replace("%allowed%", sAllowed);
        v = v.replace("%coords%", "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")");

        String sDimensions;
        if (f.hasFlag(FieldFlag.CUBOID))
        {
            sDimensions = "" + (f.getMaxx() - f.getMinx() + 1) + "x" + (f.getMaxy() - f.getMiny() + 1) + "x" + (f.getMaxz() - f.getMinz() + 1);
        }
        else
        {
            sDimensions = "" + ((f.getRadius() * 2) + 1) + "x" + f.getHeight() + "x" + ((f.getRadius() * 2) + 1);
        }
        v = v.replace("%dimensions%", sDimensions);
        v = v.replace("%flags%", f.getFlagsModule().getFlagsAsString());
        return v;
    }


    private void addStyle(AreaMarker m, Field f)
    {

        FieldStyle as = field_styles.get(f.getTypeId() + "_style");

        if (as == null)
        {
            as = default_style;
        }

        int sc = 0xFF0000;
        int fc = 0xFF0000;
        try
        {
            sc = Integer.parseInt(as.strokecolor.substring(1), 16);
            fc = Integer.parseInt(as.fillcolor.substring(1), 16);
        }
        catch (NumberFormatException nfx)
        {
        }
        m.setLineStyle(as.strokeweight, as.strokeopacity, sc);
        m.setFillStyle(as.fillopacity, fc);


    }

    private void handleFieldIcon(World world, Field field, Map<String, Marker> newMarkers)
    {
        String id = field.getId() + "_marker";
        Marker m = markers.remove(id);
        //If we're not showing this
        if (!field.hasFlag(FieldFlag.DYNMAP_MARKER))
        {
            return;
        }

        //retrieve the markericon from the icons list.
        MarkerIcon icon = icons.get(field.getTypeId() + "_style");
        Location loc = field.getLocation();
        FieldStyle fs = field_styles.get(field.getTypeId() + "_style");
        String replaced_markers = replaceSubstitutions(fs.marker_text_format, field);


        String description = Helper.colorToHTML(replaced_markers, "z-index=100000;");


        if (m == null)
        {
            m = markerSet.createMarker(id, description, true, world.getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
        }
        else
        {
            m.setLocation(world.getName(), loc.getX(), loc.getY(), loc.getZ());
            m.setLabel(description, true);
            m.setMarkerIcon(icon);
        }

        newMarkers.put(id, m);
    }

    /*
     * Handle specific Field as an Area
     */
    private void handleFieldArea(World world, Field field, Map<String, AreaMarker> newmap)
    {

        String field_id = field.getId() + "_area";
        //Do this up here in case we're toggling fields
        AreaMarker m = resareas.remove(field_id);

        //If we're not showing this
        if (!field.hasFlag(FieldFlag.DYNMAP_AREA))
        {
            return;
        }
        /*
         * Build popup
         */
        String desc = "";

        /*
         * Make outline
         */
        double[] x = new double[4];
        double[] z = new double[4];
        x[0] = field.getMaxx();
        z[0] = field.getMaxz();
        x[1] = field.getMaxx();
        z[1] = field.getMinz() + 1.0;
        x[2] = field.getMinx() + 1.0;
        z[2] = field.getMinz() + 1.0;
        x[3] = field.getMinx() + 1.0;
        z[3] = field.getMaxz();

        Location loc = field.getBlock().getLocation();
        String name = field.getSettings().getTitle() + "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";

        /*
         * Existing area?
         */
        if (m == null)
        {
            m = markerSet.createAreaMarker(field_id, name, false, world.getName(), x, z, false);
            if (m == null)
            {
                return;
            }
        }
        else
        {
            m.setCornerLocations(x, z); /*
             * Replace corner locations
             */
            m.setLabel(name);   /*
             * Update label
             */
        }
        if (use3d)
        { /*
             * If 3D?
             */
            m.setRangeY(field.getMiny() + 1.0, field.getMaxy());
        }


        /*
        * Set popup
        */
        m.setDescription(getFieldPopup(m, field));

        /*
         * Set line and fill properties Get the description from the style
         */
        addStyle(m, field);


        /*
        * Add to map
        */
        newmap.put(field_id, m);

    }


    private void updateMarkerSet()
    {
        Map<String, AreaMarker> newmap = new HashMap<String, AreaMarker>();

        /*
         *
         * Build new map
         */

        ForceFieldManager ffm = plugin.getPreciousStones().getForceFieldManager();

        for (World world : plugin.getServer().getWorlds())
        {
            List<Field> fields = ffm.getFields("*", world);
            for (Field f : fields)
            {
                handleFieldArea(world, f, newmap);
            }
        }

        /*
         * Now, review old map - anything left is gone
         */
        for (AreaMarker oldm : resareas.values())
        {
            oldm.deleteMarker();
        }

        // clean and replace the marker set
        /*
         * And replace with new map
         */
        resareas.clear();
        resareas = newmap;
    }
    /*
     * This class borrowed from Dynmap-WorldGuard. Thanks!
     */

    private static class FieldStyle
    {

        String strokecolor;
        double strokeopacity;
        int strokeweight;
        String fillcolor;
        double fillopacity;
        String area_text_format;
        String marker_text_format;
        String icon;

        FieldStyle(FileConfiguration cfg, String path)
        {
            strokecolor = cfg.getString(path + ".strokeColor", "#FF0000");
            strokeopacity = cfg.getDouble(path + ".strokeOpacity", 0.8);
            strokeweight = cfg.getInt(path + ".strokeWeight", 3);
            fillcolor = cfg.getString(path + ".fillColor", "#FF0000");
            fillopacity = cfg.getDouble(path + ".fillOpacity", 0.3);
            area_text_format = cfg.getString(path + ".area_text");
            marker_text_format = cfg.getString(path + ".marker_text");
            icon = cfg.getString(path + ".icon", "");
        }

        FieldStyle(FileConfiguration cfg, String path, FieldStyle def)
        {
            strokecolor = cfg.getString(path + ".strokeColor", def.strokecolor);
            strokeopacity = cfg.getDouble(path + ".strokeOpacity", def.strokeopacity);
            strokeweight = cfg.getInt(path + ".strokeWeight", def.strokeweight);
            fillcolor = cfg.getString(path + ".fillColor", def.fillcolor);
            fillopacity = cfg.getDouble(path + ".fillOpacity", def.fillopacity);
            area_text_format = cfg.getString(path + ".area_text", def.area_text_format);
            marker_text_format = cfg.getString(path + ".marker_text", def.marker_text_format);
            icon = cfg.getString(path + ".icon", def.icon);
        }
    }
}
