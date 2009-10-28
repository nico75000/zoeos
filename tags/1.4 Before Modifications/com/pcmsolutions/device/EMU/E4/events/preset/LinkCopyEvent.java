package com.pcmsolutions.device.EMU.E4.events.preset;

/**
 * User: paulmeehan
 * Date: 23-Mar-2004
 * Time: 21:28:17
 */
public class LinkCopyEvent extends LinkAddEvent {
    private Integer srcPreset;
    private Integer srcLink;

    public LinkCopyEvent(Object source, Integer preset, Integer link, Integer sourcePreset, Integer sourceLink) {
        super(source, preset, link);
        this.srcPreset = sourcePreset;
        this.srcLink = sourceLink;
    }   

    public Integer getSrcPreset() {
        return srcPreset;
    }

    public Integer getSrcLink() {
        return srcLink;
    }
}
