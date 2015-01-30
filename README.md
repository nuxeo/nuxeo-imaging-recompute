# Nuxeo Imaging Recompute

This is a simple plugin used to recompute the picture views of a Picture.

## Usage


### On Picture Summary

An action exists to recompute the current document picture views, but it's disabled by default.

To enable it, you can add the following contribution:

```xml
<require>org.nuxeo.ecm.imaging.recompute.actions</require>
<extension target="org.nuxeo.ecm.platform.actions.ActionService"
  point="actions">
  <action id="recomputePictureViews" enabled="true" />
</extension>
```

### On Admin Center

A new section "Imaging" is available in which an administrator can launch the recomputation of a set of Picture through a NXQL query. The recomputation is done in an asynchronous way.




## About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com/).

