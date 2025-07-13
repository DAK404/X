/*
*                                                      |
*                                                     ||
*  |||||| ||||||||| |||||||| ||||||||| |||||||  |||  ||| ||||||| |||||||||  |||||| |||||||||
* |||            ||    |||          ||       || |||  |||       ||       || |||        |||
* |||      ||||||||    |||    ||||||||  ||||||  ||||||||  ||||||  |||||||| |||        |||
* |||      |||  |||    |||    |||  |||  |||     |||  |||  ||  ||  |||  ||| |||        |||
*  ||||||  |||  |||    |||    |||  |||  |||     |||  |||  ||   || |||  |||  ||||||    |||
*                                               ||
*                                               |
*
* A Cross Platform OS Shell
* Powered By Truncheon Core
*/

/*
 * This file is part of the Cataphract project.
 * Copyright (C) 2024 DAK404 (https://github.com/DAK404)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package Cataphract.API.Wraith.Archive;

import java.nio.file.Path;

/**
 * Interface for archive operations (e.g., zip, unzip).
 *
 * @author DAK404 (https://github.com/DAK404)
 * @version 1.2.0 (13-July-2025, Cataphract)
 * @since 0.0.1 (Cataphract 0.0.1)
 */
public interface ArchiveHandler {
    /**
     * Compresses a directory or file into an archive.
     *
     * @param sourcePath      Path to the source file or directory.
     * @param destinationPath Path to the output archive file.
     * @throws Exception If an error occurs during compression.
     */
    void compress(Path sourcePath, Path destinationPath) throws Exception;

    /**
     * Decompresses an archive to the specified destination.
     *
     * @param archivePath     Path to the archive file.
     * @param destinationPath Path to the output directory.
     * @throws Exception If an error occurs during decompression.
     */
    void decompress(Path archivePath, Path destinationPath) throws Exception;

    /**
     * Installs an update by decompressing a predefined archive.
     *
     * @throws Exception If an error occurs during update installation.
     */
    void installUpdate() throws Exception;
}