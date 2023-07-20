# Save BChecks to File
#### This extension retrieves raw BCheck content from GitHub and saves it to a local folder for easier import.

---

This is a first iteration.
## Usage
1. Set folder location in `MyBurpExtension.java`
2. Build extension
3. Load extension into Burp
4. Go to `Extensions > BChecks > Import` and select configured folder

## Improvements

- Some BCheck files throw errors
- UI to configure locations
- Recurse GitHub directories rather than manually specifying
- Use threads to maintain responsiveness in Burp