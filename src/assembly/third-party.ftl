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
 
 - Font Awesome by Dave Gandy (http://fontawesome.io)
     License: (http://fontawesome.io/license/)
       - Font License: SIL OFL 1.1 (http://scripts.sil.org/OFL)
       - Code License: MIT License (http://opensource.org/licenses/mit-license.html)

 - Material Design Lite (http://www.getmdl.io/)
     License: The Apache Software License, Version 2.0 (https://raw.githubusercontent.com/google/material-design-lite/master/LICENSE)
     
 - Bootstrap (http://getbootstrap.com/)
     License: MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
     
 - Spectrum Colorpicker (https://github.com/bgrins/spectrum)
     License: MIT (https://github.com/bgrins/spectrum/blob/master/LICENSE)
 
 - AngularJS (http://angularjs.org)
     License: MIT (https://github.com/angular/angular.js/blob/master/LICENSE)

  - Chart.js (http://chartjs.org/)
     License: MIT (https://github.com/nnnick/Chart.js/blob/master/LICENSE.md)