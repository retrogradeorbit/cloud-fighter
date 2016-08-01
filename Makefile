CSS=build/css/style.css
APP=build/js/compiled/cloud_fighter.js
IDX=build/index.html
IMG=build/img/sprites.png build/img/fonts.png
IMG_PUBLIC=$(subst build,resources/public,$(IMG))
SFX_SOURCE=$(wildcard resources/public/sfx/*.ogg)
SFX=$(subst resources/public,build,$(SFX_SOURCE))
ME=$(shell basename $(shell pwd))
REPO=git@github.com:retrogradeorbit/cloud-fighter.git

all: $(APP) $(CSS) $(IDX) $(IMG) $(SFX) $(MUSIC)

$(CSS): resources/public/css/style.css
	mkdir -p $(dir $(CSS))
	cp $< $@

$(APP): src/**/** project.clj
	rm -f $(APP)
	lein cljsbuild once min

$(IDX): resources/public/index.html
	cp $< $@

$(IMG): $(IMG_PUBLIC)
	mkdir -p build/img/
	cp $? build/img/

$(SFX): $(SFX_SOURCE)
	mkdir -p build/sfx/
	cp $? build/sfx/

clean:
	lein clean
	rm -rf $(CSS) $(APP) $(IDX) $(IMG) $(SFX) $(MUSIC)

test-server: all
	cd build && python -m SimpleHTTPServer

setup-build-folder:
	git clone $(REPO) build/
	cd build && git checkout gh-pages

create-build-folder:
	git clone $(REPO) build/
