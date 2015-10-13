package hudson.plugins.dry;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.FileAnnotation;
import org.jenkinsci.plugins.codehealth.provider.duplicates.DuplicateCode;
import org.jenkinsci.plugins.codehealth.provider.duplicates.DuplicateCodeDescriptor;
import org.jenkinsci.plugins.codehealth.provider.duplicates.DuplicateCodeProvider;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Prankl
 */
public class DryDuplicateCodeProvider extends DuplicateCodeProvider {

    @DataBoundConstructor
    public DryDuplicateCodeProvider() {
    }

    @Override
    public DuplicateCode getDuplicateCode(AbstractBuild<?, ?> build) {
        DryResultAction dryResultAction = build.getAction(DryResultAction.class);
        if (dryResultAction != null) {
            List<Integer> processedDryNumbers = new ArrayList<Integer>();
            List<String> processedFiles = new ArrayList<String>();
            int lineCount = 0;
            Set<FileAnnotation> annotations = dryResultAction.getResult().getAnnotations();
            for (FileAnnotation fileAnnotation : annotations) {
                if (fileAnnotation instanceof hudson.plugins.dry.parser.DuplicateCode) {
                    hudson.plugins.dry.parser.DuplicateCode dup = (hudson.plugins.dry.parser.DuplicateCode) fileAnnotation;
                    if (!processedDryNumbers.contains(dup.getNumber())) {
                        processedDryNumbers.add(dup.getNumber());
                        lineCount += dup.getNumberOfLines();
                        for (hudson.plugins.dry.parser.DuplicateCode linkedDup : dup.getLinks()) {
                            if (!processedDryNumbers.contains(linkedDup.getNumber())) {
                                processedDryNumbers.add(linkedDup.getNumber());
                                lineCount += linkedDup.getNumberOfLines();
                            }
                            if (!processedFiles.contains(linkedDup.getFileName())) {
                                processedFiles.add(linkedDup.getFileName());
                            }
                        }
                    }
                    if (!processedFiles.contains(fileAnnotation.getFileName())) {
                        processedFiles.add(fileAnnotation.getFileName());
                    }
                }
            }
            DuplicateCode duplicateCode = new DuplicateCode(lineCount, processedFiles.size());
            return duplicateCode;
        }
        return null;
    }


    @Override
    public String getOrigin() {
        return "DRY";
    }

    @Override
    @Nullable
    public String getBuildResultUrl() {
        return DryDescriptor.createResultUrlName(DryDescriptor.PLUGIN_ID);
    }

    @Nullable
    @Override
    public String getProjectResultUrl() {
        return DryDescriptor.PLUGIN_ID;
    }

    @Override
    public DuplicateCodeDescriptor getDescriptor() {
        return super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends DuplicateCodeDescriptor {

        @Override
        public String getDisplayName() {
            return "DRY";
        }

    }
}