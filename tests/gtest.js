var out = scripting.getWriter();
var err = scripting.getErrorWriter();

scripting.importClass
    ("org.bzdev.gio.OutputStreamGraphics");
scripting.importClasses("org.bzdev.graphs",
                        ["Graph", "AxisBuilder"]);
scripting.importClasses("org.bzdev.math.rv",
                        ["GaussianRV", "PoissonIntegerRV"]);
scripting.importClass("org.bzdev.util.units.MKS");

// Import Java classes
scripting.importClass("java.awt.geom.Path2D");
scripting.importClass("java.awt.Color");
scripting.importClass("java.awt.BasicStroke");

// Set the running time if was not provided on
// the command line:
// scrunner ... -vD:runningTime:TIME_IN_HOURS
var runningTime;
if (typeof runningTime == "undefined") {
    runningTime = 8.0;
}
// convert from hours to seconds.
runningTime = MKS.hours(runningTime);

var osg;
var gout;
if (typeof gout == "undefined"
    || typeof gtype == "undefined") {
   osg = null;
} else {
    // Set up a graph so that it is written
    //  to the output stream named gout.
    osg = OutputStreamGraphics.newInstance
              (gout, 800, 600, gtype);
}

var g2d = null;
if (osg != null) {
    graph = new Graph(osg);
    graph.setRanges(0.0, runningTime, 0.0, 10.0);
    graph.setOffsets(100, 50, 100, 50);
    xab = new AxisBuilder.ClockTime
           (graph, 0.0, 0.0, runningTime, true,
            "Time (HH:MM)");
    xab.setSpacings(AxisBuilder.Spacing.MINUTES,
                   AxisBuilder.Spacing.HOURS);
    xab.addTickSpec
      (0, AxisBuilder.Spacing.HOURS, "%1$TR");
    xab.addTickSpec
      (1, AxisBuilder.Spacing.THIRTY_MINUTES, null);
    xab.addTickSpec
      (2, AxisBuilder.Spacing.TEN_MINUTES, null);
    graph.draw(xab.createAxis());
    yab = new AxisBuilder.Linear
           (graph, 0.0, 0.0, 10.0, false, "Count");
    yab.setMaximumExponent(1);
    yab.addTickSpec(0, 0, true, "%3.0f", "%3.0f");
    yab.addTickSpec(2, 1, false, null);
    graph.draw(yab.createAxis());
    graph.write();
}
