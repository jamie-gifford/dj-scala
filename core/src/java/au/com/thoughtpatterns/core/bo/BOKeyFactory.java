package au.com.thoughtpatterns.core.bo;


public class BOKeyFactory {

    public static <T extends IEntity> BOKey<T> createKey(Class<T> clas, Long id) {
      return new BOKeyImpl<T>(clas, id);
  }

}
