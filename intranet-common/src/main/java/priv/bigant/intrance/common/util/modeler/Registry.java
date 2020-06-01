/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package priv.bigant.intrance.common.util.modeler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.util.modeler.modules.ModelerSource;

import javax.management.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/*
   Issues:
   - exceptions - too many "throws Exception"
   - double check the interfaces
   - start removing the use of the experimental methods in tomcat, then remove
     the methods ( before 1.1 final )
   - is the security enough to prevent Registry being used to avoid the permission
    checks in the mbean server ?
*/

/**
 * Registry for modeler MBeans.
 * <p>
 * This is the main entry point into modeler. It provides methods to create and manipulate model mbeans and simplify
 * their use.
 * <p>
 * This class is itself an mbean.
 * <p>
 * IMPORTANT: public methods not marked with @since x.x are experimental or internal. Should not be used.
 *
 * @author Craig R. McClanahan
 * @author Costin Manolache
 */
public class Registry implements MBeanRegistration {
    /**
     * The Log instance to which we will write our log messages.
     */
    private static final Logger log = LoggerFactory.getLogger(Registry.class);

    // Support for the factory methods

    /**
     * Will be used to isolate different apps and enhance security.
     */
    private static final HashMap<Object, Registry> perLoaderRegistries = null;

    /**
     * The registry instance created by our factory method the first time it is called.
     */
    private static Registry registry = null;

    // Per registry fields

    /**
     * The <code>MBeanServer</code> instance that we will use to register management beans.
     */
    private MBeanServer server = null;

    /**
     * The set of ManagedBean instances for the beans this registry knows about, keyed by name.
     */
    private HashMap<String, ManagedBean> descriptors = new HashMap<>();

    /**
     * List of managed beans, keyed by class name
     */
    private HashMap<String, ManagedBean> descriptorsByClass = new HashMap<>();

    // map to avoid duplicated searching or loading descriptors
    private HashMap<String, URL> searchedPaths = new HashMap<>();

    private Object guard;



    // ----------------------------------------------------------- Constructors

    /**
     */
    public Registry() {
        super();
    }

    // -------------------- Static methods  --------------------
    // Factories

    /**
     * Factory method to create (if necessary) and return our
     * <code>Registry</code> instance.
     * <p>
     * The current version uses a static - future versions could use the thread class loader.
     *
     * @param key   Support for application isolation. If null, the context class loader will be used ( if
     *              setUseContextClassLoader is called ) or the default registry is returned.
     * @param guard Prevent access to the registry by untrusted components
     * @return the registry
     * @since 1.1
     */
    public static synchronized Registry getRegistry(Object key, Object guard) {
        Registry localRegistry;
        if (perLoaderRegistries != null) {
            if (key == null)
                key = Thread.currentThread().getContextClassLoader();
            if (key != null) {
                localRegistry = perLoaderRegistries.get(key);
                if (localRegistry == null) {
                    localRegistry = new Registry();
//                    localRegistry.key=key;
                    localRegistry.guard = guard;
                    perLoaderRegistries.put(key, localRegistry);
                    return localRegistry;
                }
                if (localRegistry.guard != null && localRegistry.guard != guard) {
                    return null; // XXX Should I throw a permission ex ?
                }
                return localRegistry;
            }
        }

        // static
        if (registry == null) {
            registry = new Registry();
        }
        if (registry.guard != null && registry.guard != guard) {
            return null;
        }
        return (registry);
    }

    // -------------------- Generic methods  --------------------


    // -------------------- ID registry --------------------

    // -------------------- Metadata   --------------------
    // methods from 1.0

    /**
     * Add a new bean metadata to the set of beans known to this registry. This is used by internal components.
     *
     * @param bean The managed bean to be added
     * @since 1.0
     */
    public void addManagedBean(ManagedBean bean) {
        // XXX Use group + name
        descriptors.put(bean.getName(), bean);
        if (bean.getType() != null) {
            descriptorsByClass.put(bean.getType(), bean);
        }
    }


    /**
     * Find and return the managed bean definition for the specified bean name, if any; otherwise return
     * <code>null</code>.
     *
     * @param name Name of the managed bean to be returned. Since 1.1, both short names or the full name of the class
     *             can be used.
     * @return the managed bean
     * @since 1.0
     */
    public ManagedBean findManagedBean(String name) {
        // XXX Group ?? Use Group + Type
        ManagedBean mb = descriptors.get(name);
        if (mb == null)
            mb = descriptorsByClass.get(name);
        return mb;
    }

    // -------------------- Helpers  --------------------

    /**
     * Factory method to create (if necessary) and return our
     * <code>MBeanServer</code> instance.
     *
     * @return the MBean server
     */
    public synchronized MBeanServer getMBeanServer() {
        if (server == null) {
            long t1 = System.currentTimeMillis();
            if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
                server = MBeanServerFactory.findMBeanServer(null).get(0);
                if (log.isDebugEnabled()) {
                    log.debug("Using existing MBeanServer " + (System.currentTimeMillis() - t1));
                }
            } else {
                server = ManagementFactory.getPlatformMBeanServer();
                if (log.isDebugEnabled()) {
                    log.debug("Creating MBeanServer" + (System.currentTimeMillis() - t1));
                }
            }
        }
        return server;
    }

    /**
     * Find or load metadata.
     *
     * @param bean      The bean
     * @param beanClass The bean class
     * @param type      The registry type
     * @return the managed bean
     * @throws Exception An error occurred
     */
    public ManagedBean findManagedBean(Object bean, Class<?> beanClass, String type) throws Exception {
        if (bean != null && beanClass == null) {
            beanClass = bean.getClass();
        }

        if (type == null) {
            type = beanClass.getName();
        }

        // first look for existing descriptor
        ManagedBean managed = findManagedBean(type);

        // Search for a descriptor in the same package
        if (managed == null) {
            // check package and parent packages
            if (log.isDebugEnabled()) {
                log.debug("Looking for descriptor ");
            }
            findDescriptor(beanClass, type);

            managed = findManagedBean(type);
        }

        // Still not found - use introspection
        if (managed == null) {
            if (log.isDebugEnabled()) {
                log.debug("Introspecting ");
            }

            // introspection
            load("MbeansDescriptorsIntrospectionSource", beanClass, type);

            managed = findManagedBean(type);
            if (managed == null) {
                log.warn("No metadata found for " + type);
                return null;
            }
            managed.setName(type);
            addManagedBean(managed);
        }
        return managed;
    }


    /**
     * Experimental. Load descriptors.
     *
     * @param sourceType The source type
     * @param source     The bean
     * @param param      A type to load
     * @return List of descriptors
     * @throws Exception Error loading descriptors
     */
    public List<ObjectName> load(String sourceType, Object source,
                                 String param) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("load " + source);
        }
        String location = null;
        String type = null;
        Object inputsource = null;

        if (source instanceof URL) {
            URL url = (URL) source;
            location = url.toString();
            type = param;
            inputsource = url.openStream();
            if (sourceType == null && location.endsWith(".xml")) {
                sourceType = "MbeansDescriptorsDigesterSource";
            }
        } else if (source instanceof File) {
            location = ((File) source).getAbsolutePath();
            inputsource = new FileInputStream((File) source);
            type = param;
            if (sourceType == null && location.endsWith(".xml")) {
                sourceType = "MbeansDescriptorsDigesterSource";
            }
        } else if (source instanceof InputStream) {
            type = param;
            inputsource = source;
        } else if (source instanceof Class<?>) {
            location = ((Class<?>) source).getName();
            type = param;
            inputsource = source;
            if (sourceType == null) {
                sourceType = "MbeansDescriptorsIntrospectionSource";
            }
        }

        if (sourceType == null) {
            sourceType = "MbeansDescriptorsDigesterSource";
        }
        ModelerSource ds = getModelerSource(sourceType);
        List<ObjectName> mbeans = ds.loadDescriptors(this, type, inputsource);

        return mbeans;
    }


    /**
     * Register a component
     *
     * @param bean  The bean
     * @param oname The object name
     * @param type  The registry type
     * @throws Exception Error registering component
     */
    public void registerComponent(Object bean, ObjectName oname, String type) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Managed= " + oname);
        }

        if (bean == null) {
            log.error("Null component " + oname);
            return;
        }

        try {
            if (type == null) {
                type = bean.getClass().getName();
            }

            ManagedBean managed = findManagedBean(null, bean.getClass(), type);

            // The real mbean is created and registered
            DynamicMBean mbean = managed.createMBean(bean);

            if (getMBeanServer().isRegistered(oname)) {
                if (log.isDebugEnabled()) {
                    log.debug("Unregistering existing component " + oname);
                }
                getMBeanServer().unregisterMBean(oname);
            }

            getMBeanServer().registerMBean(mbean, oname);
        } catch (Exception ex) {
            log.error("Error registering " + oname, ex);
            throw ex;
        }
    }

    /**
     * Lookup the component descriptor in the package and in the parent packages.
     *
     * @param packageName The package name
     * @param classLoader The class loader
     */
    public void loadDescriptors(String packageName, ClassLoader classLoader) {
        String res = packageName.replace('.', '/');

        if (log.isTraceEnabled()) {
            log.trace("Finding descriptor " + res);
        }

        if (searchedPaths.get(packageName) != null) {
            return;
        }

        String descriptors = res + "/mbeans-descriptors.xml";
        URL dURL = classLoader.getResource(descriptors);

        if (dURL == null) {
            return;
        }

        log.debug("Found " + dURL);
        searchedPaths.put(packageName, dURL);
        try {
            load("MbeansDescriptorsDigesterSource", dURL, null);
        } catch (Exception ex) {
            log.error("Error loading " + dURL);
        }
    }

    /**
     * Lookup the component descriptor in the package and in the parent packages.
     */
    private void findDescriptor(Class<?> beanClass, String type) {
        if (type == null) {
            type = beanClass.getName();
        }
        ClassLoader classLoader = null;
        if (beanClass != null) {
            classLoader = beanClass.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        String className = type;
        String pkg = className;
        while (pkg.indexOf(".") > 0) {
            int lastComp = pkg.lastIndexOf(".");
            if (lastComp <= 0) return;
            pkg = pkg.substring(0, lastComp);
            if (searchedPaths.get(pkg) != null) {
                return;
            }
            loadDescriptors(pkg, classLoader);
        }
        return;
    }

    private ModelerSource getModelerSource(String type) throws Exception {
        if (type == null)
            type = "MbeansDescriptorsDigesterSource";
        if (type.indexOf(".") < 0) {
            type = "priv.bigant.intrance.common.util.modeler.modules." + type;
        }
        Class<?> c = Class.forName(type);
        return (ModelerSource) c.getConstructor().newInstance();
    }


    // -------------------- Registration  --------------------

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        this.server = server;
        return name;
    }

    @Override
    public void postRegister(Boolean registrationDone) {
    }

    @Override
    public void preDeregister() throws Exception {
    }

    @Override
    public void postDeregister() {
    }
}
