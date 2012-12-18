srcdir:=../../data/config-old
dstdir:=../../data/config
srcconfs:=$(wildcard $(srcdir)/*.xml)
dstconfs:=$(srcconfs:$(srcdir)/%.xml=$(dstdir)/%.xml)
xslfile:=translator.xsl

all: $(dstconfs)

$(dstconfs): $(dstdir)/%: $(srcdir)/% $(xslfile)
	mkdir -p $(dstdir)
	xsltproc -o $@ $(xslfile) $<

