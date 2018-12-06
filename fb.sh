#!/bin/bash



$FLATBUFFERS_DIR/flatbuffers/flatc -o src/main/java/ --java --gen-mutable src/main/resources/messages.fbs
