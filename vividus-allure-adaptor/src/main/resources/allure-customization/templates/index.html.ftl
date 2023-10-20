<!DOCTYPE html>
<html dir="ltr">
<head>
    <meta charset="utf-8">
    <title>Allure Report</title>
    <link rel="favicon" href="favicon.ico?v=3">
    <link rel="stylesheet" type="text/css" href="styles.css">
    <#list plugins as plugin>
        <#list plugin.cssFiles as cssFile>
            <link rel="stylesheet" href="plugins/${plugin.id}/${cssFile}">
        </#list>
    </#list>
</head>
<body>
<div id="alert"></div>
<div id="content">
    <span class="spinner">
        <span class="spinner__circle"></span>
    </span>
</div>
<div id="popup"></div>
<script src="app.js"></script>
<#list plugins as plugin>
    <#list plugin.jsFiles as jsFile>
        <script src="plugins/${plugin.id}/${jsFile}"></script>
    </#list>
</#list>
<#--<#if ALLURE_NO_ANALYTICS == false>-->
<#--    <script async src="https://www.googletagmanager.com/gtag/js?id=G-FVWC4GKEYS"></script>-->
<#--</#if>-->
<script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
    gtag('config', 'G-FVWC4GKEYS');
    gtag('allureVersion', '${allureVersion}')
    gtag('reportUuid', '${reportUuid}')
</script>
</body>
</html>