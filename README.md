# Accel 🗺️📍

Accel is an Android application for managing and tracking locations with ease! 🌍🚀

## 🌟 Features
- 🗺️ View locations on an interactive map
- 📋 Manage your locations list
- 💾 Store locations in local database

## 🏗️ Project Structure
- `MainActivity`: 🚪 Entry point of the application
- `MapFragment`: 🗺️ Displays locations on a map
- `LocationsFragment`: 📝 Shows list of locations
- `DatabaseHelper`: 💾 Handles local SQLite database operations

## 🛠️ Requirements
- Android SDK 34
- Java 17

## 🚀 Building
1. 📦 Clone the repository
2. 🖥️ Open project in Android Studio
3. 🏃 Build and run the app

## 🔤 Caskaydia Fonts

This project uses Caskaydia Cove Nerd Fonts. We provide three font variants:

### Font Variants
- **Cove**: Standard typeface with various weights
- **Mono**: Monospaced variant
- **Propo**: Proportional variant

### Weights Available
- Extralight
- Light
- Semilight
- Regular
- Semibold
- Bold

### Installation Instructions

#### Android Studio
1. Copy font files from `app/src/main/res/font/` to your project
2. Reference fonts in XML layouts using:
   ```xml
   android:fontFamily="@font/caskaydiacovenerdfont_regular"
   ```

#### Desktop/Manual Installation
1. Download from [Nerd Fonts Website](https://www.nerdfonts.com/font-downloads)
2. Install by double-clicking the `.ttf` files
3. Select desired variant (Cove, Mono, Propo)

### Renaming Font Files

To rename all font files to lowercase and replace special characters, use this command:

```bash
cd app/src/main/res/font && 
for file in *.ttf; do 
    new_name=$(echo "$file" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.]/_/g'); 
    mv "$file" "$new_name"; 
done
```
This will fix a build error related to the file names.

This script will:
- Convert all filenames to lowercase
- Replace any non-alphanumeric characters (except `.` and `_`) with underscores
- Rename the files in-place

After running, your font files will have consistent, clean names.

## 📱 Screenshots
[Demo](Accel.gif)

## 🤝 Contributing
Contributions are welcome! Feel free to open issues or submit pull requests.

## 📄 License

MIT License

Copyright (c) 2025 Accel Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.