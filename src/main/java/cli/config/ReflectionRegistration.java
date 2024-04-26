//package cli.config;
//
//
//import com.oracle.svm.core.annotate.AutomaticFeature;
//import com.oracle.svm.core.feature.AutomaticallyRegisteredFeature;
//import com.oracle.svm.util.ReflectionUtil;
//import org.apache.lucene.index.ConcurrentMergeScheduler;
//import org.graalvm.nativeimage.hosted.Feature;
//import org.graalvm.nativeimage.hosted.RuntimeReflection;
//
//import java.util.Map;
//
//
//@AutomaticFeature
//public class ReflectionRegistration implements Feature {
//
//    public void beforeAnalysis(BeforeAnalysisAccess access) {
//        try {
//            RuntimeReflection.register(ReflectionUtil.lookupConstructor(ConcurrentMergeScheduler.class, Map.class));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
