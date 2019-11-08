/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

#ifndef __ZLFILEINFO_H__
#define __ZLFILEINFO_H__

struct ZLFileInfo {
	bool Exists;
	bool IsDirectory;
	std::size_t Size;
	std::size_t MTime;

	ZLFileInfo();
};

inline ZLFileInfo::ZLFileInfo() : Exists(false), IsDirectory(false), Size(0) {
}

#endif /* __ZLFILEINFO_H__ */
