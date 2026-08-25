/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

/** Cable-bus part forms available to a data-only extension registration. */
public enum CableBusPartKind {
    /** One or more static model layers with no retained spin or frequency. */
    STATIC,
    /** AE2's static-off P2P shell plus a retained unsigned-short frequency. */
    P2P
}
