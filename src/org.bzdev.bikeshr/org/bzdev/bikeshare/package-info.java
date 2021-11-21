/**
 * Bike-sharing simulation package.
 * <P>
 * This package, which is based on the drama package (org.bzdev.drama),
 * provides classes that can be used to simulate a bike-sharing system.
 * The class hierarchy can be partitioned into three sections:
 * <UL>
 *   <LI> The <A href="#run">classes</A> used to run a simulation.
 *   <LI> The <A href="#factories">classes</A> used to configure a
 *        simulation (i.e., factory  classes)
 *   <LI> The <A href="#instr">classes</A> used to instrument a simulation.
 * </UL>
 *
 * <A anchor="run"><H2>Classes used to run a simulation</H2></A>
 * <P>
 * The classes used during the execution of a simulation, excluding
 * any instrumentation, are shown in the following figure:
 * <P style="text-align: center">
 * <img src="doc-files/simclasses.png" class="imgBackground">
 * <P>
 *
 * <A anchor="factories"><H2>Classes used to configure a simulation</H2></A>
 * <P>
 * Factory classes are used to configure simulations by creating
 * simulation objects.  The factories that create actors are shown
 * in the following figure:
 * <P style="text-align: center">
 * <img src="doc-files/factories1.png" class="imgBackground">
 * <P>
 * Similarly, the factories that configure domains and
 * delay tables are shown in the following figure:
 * <P style="text-align: center">
 * <img src="doc-files/factories2.png" class="imgBackground">
 * <P>
 * As mentioned in the top-level overview, the factories should be
 * used in the following order or a similar order:
 * <UL>
 *    <LI> {@link org.bzdev.bikeshare.ExtDomainFactory ExtDomainFactory}.
 *    <LI> {@link org.bzdev.bikeshare.UsrDomainFactory UsrDomainFactory}.
 *    <LI> {@link org.bzdev.bikeshare.SysDomainFactory SysDomainFactory}.
 *    <LI>
 *  {@link org.bzdev.bikeshare.BasicHubBalancerFactory BasicHubBalancerFactory}.
 *    <LI> {@link org.bzdev.bikeshare.HubFactory HubFactory}.
 *    <LI> {@link org.bzdev.bikeshare.StorageHubFactory StorageHubFactory}.
 *    <LI> {@link org.bzdev.bikeshare.HubWorkerFactory HubWorkerFactory}.
 *    <LI> {@link org.bzdev.bikeshare.StdDelayTableFactory StdDelayTableFactory}.
 *    <LI> {@link org.bzdev.bikeshare.SchedDelayTableFactory SchedDelayTableFactory}
 *    <LI> subclasses of
 *         {@link org.bzdev.bikeshare.TripGeneratorFactory TripGeneratorFactory}:
 *         <UL>
 *         <LI> {@link org.bzdev.bikeshare.BasicTripGenFactory BasicTripGenFactory}
 *         <LI> {@link org.bzdev.bikeshare.BurstTripGenFactory BurstTripGenFactory}
 *         </UL>
 * </UL>
 * Regardless of the ordering, it must be such that objects are
 * created before they are referenced, and some factories set or add
 * parameters whose values are objects created by other factories:
 * <UL>
 *   <LI>{@link org.bzdev.bikeshare.UsrDomainFactory UsrDomainFactory} has
 *      a parameter that is an external domain (the default is null, which
 *      is a legal value).
 *   <LI>
 *   {@link org.bzdev.bikeshare.BasicHubBalancerFactory BasicHubBalancerFactory}
 *         has a parameter whose value is a system domain.
 *   <LI> {@link org.bzdev.bikeshare.HubFactory HubFactory} has parameters
 *        whose values are a user domain and a system domain.
 *   <LI> {@link org.bzdev.bikeshare.StorageHubFactory  StorageHubFactory}
 *         has parameters whose values are a system domain and a list of hubs.
 *   <LI> {@link org.bzdev.bikeshare.HubWorkerFactory HubWorkerFactory}
 *        has parameters whose values are a system domain, and a storage hub.
 *   <LI> {@link org.bzdev.bikeshare.StdDelayTableFactory StdDelayTableFactory}
 *        and
 *     {@link org.bzdev.bikeshare.SchedDelayTableFactory SchedDelayTableFactory}
 *        have parameters whose values are hub domains and hubs.
 *   <LI> {@link org.bzdev.bikeshare.BasicTripGenFactory BasicTripGenFactory}
 *        and
 *        {@link org.bzdev.bikeshare.BurstTripGenFactory BurstTripGenFactory}
 *        has parameters whose values are hubs.
 * </UL>
 *
 * <A anchor="instr"><H2>Classes used to instrument a simulation</H2></A>
 * Classes used to instrument simulations can use instances of the
 * classes shown in the following figure:
 * <P style="text-align: center">
 * <img src="doc-files/instrument.png" class="imgBackground">
 * <P>
 * For simulations written totally in Java, one could use the interfaces
 * directly although using the adapters may be more convenient in cases
 * were only a subset of the methods for the corresponding interface
 * are used to record or process various events.  In this case, when
 * the adapters are used, one would typically use their zero-argument
 * constructors and provide custom implementations for whatever methods
 * are useful.  For simulations that are written in a scripting language
 * there are two options:
 * <UL>
 *   <LI> Create Java classes that implement the listeners or extend the
 *        adapters.
 *   <LI> Use the two argument constructors for the adapters and provide
 *        a scripting-language object that implements the adapters'
 *        behavior (in this case, when scrunner is used, the first
 *        argument for an adapter's constructor will be the predefined
 *        variable <code>scripting</code> and the second argument will
 *        be a scripting-language object whose methods provide the
 *        implementation for the methods defined by the corresponding
 *        listener.
 * </UL>
 */
package org.bzdev.bikeshare;
//  LocalWords:  href img src UsrDomainFactory SysDomainFactory
//  LocalWords:  BasicHubBalancerFactory HubFactory StorageHubFactory
//  LocalWords:  HubWorkerFactory DelayTableFactory scrunner
//  LocalWords:  BasicTripGenFactory ExtDomainFactory
//  LocalWords:  StdDelayTableFactory SchedDelayTableFactory
//  LocalWords:  TripGeneratorFactory BurstTripGenFactory
