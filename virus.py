import os
import win32api

# Get a list of all available drives
drives = win32api.GetLogicalDriveStrings()
drives = drives.split('\000')[:-1]

# Iterate through the drives excluding the C drive
for drive in drives:
    if drive != 'C:\\':
        print(f"Drive: {drive}")

        # Iterate through all files and folders in the drive
        for root, directories, files in os.walk(drive):
            for file in files:
                # Check if the file has a .txt extension
                if file.endswith('.txt'):
                    # Print the file path or perform any other desired operation
                    print(os.path.join(root, file))