/**
 * Module containing classes for a bicycle-sharing simulation.
 */
module org.bzdev.bikeshr {
    exports org.bzdev.bikeshare;
    opens org.bzdev.bikeshare.lpack;
    requires java.base;
    requires java.desktop;
    requires org.bzdev.base;
    requires org.bzdev.obnaming;
    requires org.bzdev.desktop;
    requires org.bzdev.devqsim;
    requires org.bzdev.drama;
    requires static org.bzdev.dmethods;
    requires static org.bzdev.parmproc;
    provides org.bzdev.obnaming.NamedObjectFactory with
	org.bzdev.bikeshare.BasicHubBalancerFactory,
	org.bzdev.bikeshare.BasicTripGenFactory,
	org.bzdev.bikeshare.BurstTripGenFactory,
	org.bzdev.bikeshare.RoundTripGenFactory,
	org.bzdev.bikeshare.SchedDelayTableFactory,
	org.bzdev.bikeshare.StdDelayTableFactory,
	org.bzdev.bikeshare.HubFactory,
	org.bzdev.bikeshare.HubWorkerFactory,
	org.bzdev.bikeshare.StorageHubFactory,
	org.bzdev.bikeshare.ExtDomainFactory,
	org.bzdev.bikeshare.SysDomainFactory,
	org.bzdev.bikeshare.UsrDomainFactory;
}
