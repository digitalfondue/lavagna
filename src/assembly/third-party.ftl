<#-- To render the third-party file.
 Available context :

 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<#function licenseProjectFormat licenses>
	<#assign result = ""/>
		<#list licenses as license>
        <#assign result = result + "     License: " + license.name + " ("+ (license.url!"no url defined") + ")\n"/>
    </#list>
	<#return result>
</#function>
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return p.artifactId + " (" + (p.url!"no url defined") +"); " + p.groupId + ":" + p.artifactId + ":" + p.version + "\n" + licenseProjectFormat(p.licenses)>
    <#else>
        <#return p.name + " ("  + (p.url!"no url defined")+ "); " + p.groupId + ":" + p.artifactId + ":" + p.version + "\n" + licenseProjectFormat(p.licenses)>
    </#if>
</#function>

This product includes/uses the following libraries:

<#list dependencyMap as e>
	<#assign project = e.getKey()/>
	<#assign licenses = e.getValue()/>
 - ${artifactFormat(project)}
</#list>
 - winsw: Windows service wrapper in less restrictive license (https://github.com/kohsuke/winsw);
     License: The MIT License (http://www.opensource.org/licenses/mit-license.php)
