# jw-intro-map

An interactive map demo introducing my GIS engineering experience, built for the
**Full Stack Developer – Pyrecast** role at Spatial Informatics Group.

**Live demo:** https://jw-gis-experience.netlify.app/

## Overview

I learned to consider maintainability, simplicity, and reliability in every
design decision I made as an engineer. This is why I thoroughly appreciate the
expressiveness and simplicity of Clojure, especially in the GIS domain, where
incidental complexity can build up rapidly.

This small single-page app plots three projects from my career on a map. Each
location carries a short write-up of the role, stack, and what I built there.
Stepping through the projects — with the arrow buttons, the ←/→ keys, the
progress dots, or by clicking a marker — flies the camera to that location and
highlights its marker.

## Why Reagent and Mapbox

**Reagent** keeps the UI as plain ClojureScript data and functions. The project
cards, header, and progress bar are all driven by a single source of truth (the
`locations` vector and a `selected-location` atom), so the interface stays
declarative and easy to reason about — the same maintainability-first values I'd
bring to a production GIS codebase.

**Mapbox GL** handles the imperative, stateful map. The interesting integration
detail is keeping these two worlds cleanly separated: the map lives in a Form-3
Reagent component (`map-view`) whose inner DOM node Reagent never re-renders, so
Mapbox retains full control of it. Camera movement and marker highlighting are
driven off a watch on `selected-location` rather than the render path, so React
and Mapbox never fight over the same DOM. A guarded `create-map!` and an `error`
listener surface a fallback message if the Mapbox token is missing or the map
fails to load.

## Built with Claude Code and Claude Design

The visual direction for this project was prototyped with **Claude Design** —
the "field notes" aesthetic, layout, and the wireframe in `design/` came out of
that exploration before being translated into Reagent components and Tailwind.

The implementation was done with **Claude Code** as a pair-programming partner:
iterating on the Reagent/Mapbox integration, the error handling around the map
lifecycle, and the Tailwind styling. The source for my broader GIS work is at
[jw-gis-experience](https://github.com/jwhite927/jw-gis-experience).

## Setup

A Mapbox public token is read at compile time from the `MAPBOX_PUBLIC_TOKEN`
environment variable. Set it before building or running:

    export MAPBOX_PUBLIC_TOKEN="pk.your_token_here"

To get an interactive development environment run:

    lein dev

This builds the Tailwind CSS once, then starts Figwheel and opens your browser
at [localhost:3449](http://localhost:3449/). Changes are hot-reloaded without a
page refresh, and you get a browser-connected REPL.

If you're adding new Tailwind classes during a session, run the watcher in a
second terminal so the stylesheet rebuilds on save:

    lein tailwind-watch

To clean all compiled files:

    lein clean

To create a production build (minified CSS + advanced ClojureScript build):

    lein build

The output is served from `resources/public/index.html`. Production builds do
not provide live reloading or a REPL.

## Stack

- **ClojureScript** + **Reagent** (React 18) for the UI
- **Mapbox GL JS** for the map
- **Tailwind CSS v4** for styling
- **Figwheel** + **Leiningen** for the dev/build tooling

## License

Copyright © 2026 Josh White

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
