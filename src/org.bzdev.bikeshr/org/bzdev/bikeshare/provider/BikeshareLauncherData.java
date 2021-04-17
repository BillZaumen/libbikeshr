package org.bzdev.bikeshare.provider;

import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class BikeshareLauncherData implements ONLauncherData {

    public BikeshareLauncherData() {}

    @Override
    public String getName() {
	return "bikeshr";
    }

    @Override
    public InputStream getInputStream() {
	return getClass().getResourceAsStream("BikeshareLauncherData.yaml");
    }

    private static final String BUNDLE
	= "org.bzdev.bikeshare.provider.lpack.BikeshareLauncherData";

    @Override
    public String description() {
	return ResourceBundle.getBundle(BUNDLE).getString("description");
    }
}
