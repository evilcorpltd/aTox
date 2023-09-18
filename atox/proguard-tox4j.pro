-keep public class im.tox.**.* extends java.lang.Enum {
    public *;
}

-keep public class * extends im.tox.tox4j.exceptions.ToxException {
    public *;
}

-keep public class scala.collection.mutable.Builder* {
    *;
}

-dontwarn javax.script.ScriptEngineFactory
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider
-dontwarn org.slf4j.impl.StaticLoggerBinder
