### Project's Structure

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {"primaryTextColor":"#fff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","lineColor":"#00a623","tertiaryColor":"#40375c","fontSize":"12px"}
  }
}%%

graph LR
  subgraph :core
    :core:domain["domain"]
    :core:navigation["navigation"]
    :core:ui["ui"]
    :core:net["net"]
    :core:data["data"]
    :core:storage["storage"]
  end
  subgraph :features
    :features:feature-graph["feature-graph"]
    :features:feature-canvas["feature-canvas"]
    :features:feature-browser["feature-browser"]
    :features:feature-file["feature-file"]
    :features:feature-file-view["feature-file-view"]
    :features:feature-settings["feature-settings"]
  end
  subgraph :pages
    :pages:page-home["page-home"]
    :pages:page-collection["page-collection"]
    :pages:page-file["page-file"]
    :pages:page-collection-row["page-collection-row"]
    :pages:page-graph["page-graph"]
    :pages:page-tabs["page-tabs"]
    :pages:page-tab["page-tab"]
    :pages:page-select-workspace["page-select-workspace"]
    :pages:page-workspace["page-workspace"]
    :pages:page-tags["page-tags"]
    :pages:page-tag["page-tag"]
  end
  :pages:page-home --> :core:domain
  :pages:page-home --> :core:navigation
  :pages:page-home --> :core:ui
  :features:feature-graph --> :core:domain
  :features:feature-graph --> :core:navigation
  :features:feature-graph --> :core:ui
  :pages:page-collection --> :core:domain
  :pages:page-collection --> :core:navigation
  :pages:page-collection --> :core:ui
  :features:feature-canvas --> :core:domain
  :features:feature-canvas --> :core:navigation
  :features:feature-canvas --> :core:ui
  :features:feature-browser --> :core:domain
  :features:feature-browser --> :core:ui
  :pages:page-file --> :core:domain
  :pages:page-file --> :core:navigation
  :pages:page-file --> :core:ui
  :pages:page-file --> :features:feature-file
  :pages:page-file --> :features:feature-file-view
  :pages:page-file --> :features:feature-graph
  :pages:page-file --> :features:feature-canvas
  :core:net --> :core:domain
  :pages:page-collection-row --> :core:domain
  :pages:page-collection-row --> :core:navigation
  :pages:page-collection-row --> :core:ui
  :pages:page-collection-row --> :features:feature-graph
  :pages:page-graph --> :core:domain
  :pages:page-graph --> :core:navigation
  :pages:page-graph --> :core:ui
  :pages:page-tabs --> :core:domain
  :pages:page-tabs --> :core:navigation
  :pages:page-tabs --> :core:ui
  :pages:page-tabs --> :pages:page-tab
  :features:feature-file --> :core:domain
  :features:feature-file --> :core:ui
  :features:feature-file --> :features:feature-file-view
  :core:data --> :core:domain
  :core:data --> :core:net
  :core:data --> :core:storage
  :core:ui --> :core:domain
  :pages:page-select-workspace --> :core:domain
  :pages:page-select-workspace --> :core:navigation
  :pages:page-select-workspace --> :core:ui
  :shared --> :core:domain
  :shared --> :core:storage
  :shared --> :core:net
  :shared --> :core:ui
  :shared --> :core:navigation
  :shared --> :core:data
  :shared --> :pages:page-workspace
  :shared --> :pages:page-select-workspace
  :pages:page-tab --> :core:domain
  :pages:page-tab --> :core:navigation
  :pages:page-tab --> :core:ui
  :pages:page-tab --> :pages:page-home
  :pages:page-tab --> :pages:page-file
  :pages:page-tab --> :pages:page-collection
  :pages:page-tab --> :pages:page-collection-row
  :pages:page-tab --> :pages:page-graph
  :pages:page-tab --> :pages:page-tags
  :pages:page-tab --> :pages:page-tag
  :core:storage --> :core:domain
  :pages:page-tags --> :core:domain
  :pages:page-tags --> :core:navigation
  :pages:page-tags --> :core:ui
  :features:feature-settings --> :core:domain
  :features:feature-settings --> :core:navigation
  :features:feature-settings --> :core:ui
  :pages:page-workspace --> :core:domain
  :pages:page-workspace --> :core:navigation
  :pages:page-workspace --> :core:ui
  :pages:page-workspace --> :pages:page-tabs
  :pages:page-workspace --> :features:feature-settings
  :pages:page-tag --> :core:domain
  :pages:page-tag --> :core:navigation
  :pages:page-tag --> :core:ui
  :pages:page-tag --> :features:feature-graph
  :core:navigation --> :core:domain
  :features:feature-file-view --> :core:domain
  :features:feature-file-view --> :core:ui
  :features:feature-file-view --> :features:feature-browser
```