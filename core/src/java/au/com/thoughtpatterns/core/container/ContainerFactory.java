package au.com.thoughtpatterns.core.container;

import au.com.thoughtpatterns.core.util.Factory;


public class ContainerFactory {

    public static Container createContainer() {
        Container container = (Container) Factory.create(Container.class);
        return container;
    }
    
}
