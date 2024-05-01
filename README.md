# lfind


`lfind` is a command line tool built entirely in java that leverages [Apache Lucene](https://lucene.apache.org/) for efficient searching across files and directories in the file system.

## Links

[Youtube link](https://youtu.be/7soDBlhvogY)  
[Google Slides](https://docs.google.com/presentation/d/1jsJ0dmrn3akUqSMIImU1bvoxlBTbZ-o4UTDCaLNB8B8/edit?usp=sharing)

## Features

- **Interactive and Direct Command Modes**: lfind supports both interactive mode for iterative search queries and direct command mode for quick one-time searches.
- **Search File Names and Content**: Easily search for file names and within file content across various file types including PDFs, Word documents, and plain text files.
- **Advanced Query Support**: Supports a wide range of query types including boolean, phrase, partial, wildcard, and prefix queries—leveraging the full querying capabilities of Lucene.
- **Piped Input Support**: Integrates seamlessly into bash scripting with support for piped inputs, allowing lfind to be combined with other commands in powerful workflows.
- **Cross-Platform Compatibility**: Works on any machine with Java installed. Tested on macOS, Windows, and expected to run smoothly on Linux environments.
- **Rich Command Line Interface**: Powered by [picocli](https://picocli.info/), lfind provides a user-friendly and feature-rich command line experience.
- **Automatic Index Cleanup**: lfind automatically cleans up index files upon safe termination of the application, ensuring efficient resource management.

## Process Flow

![img_1.png](img_1.png)

## Installation

### Requirements

- java 11 or higher (tested using [Amazon Corretto 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html))
- Tested in both windows 11 and Mac M3 Pro

### How to run

1. **Download the Package**

    - For **Windows** users, navigate to `package/windows-pkg` and run using `lfind.bat`.

    - For **Mac** users, navigate to `package/mac-pkg` and run using `lfind.sh`.

2. **Run the Application**
    - **Mac**:
      ```bash
      ./lfind.sh [options] <queries>
      ```

    - **Windows**:
      ```bash
      .\lfind.bat [options] <queries>
      ```

### Adding to Environment Path (Optional)

To access `lfind` from anywhere in the terminal, you can add the appropriate folder (`package/windows-pkg` for Windows or `package/mac-pkg` for Mac) to your system's environment path.

- **Mac**:
  ```bash
  export PATH=$PATH:/path/to/package/mac-pkg
  ```
- **Windows**:
  ```bash
  setx PATH "%PATH%;C:\path\to\package/windows-pkg"
  ```

## Usage

Ensure that you have followed the steps in the `Adding to Environment Path` section of the README. If not, run the following commands from the appropriate path.



### Running Direct Commands

```bash
lfind [options] <query>
```

### Running in Interactive Mode

```bash
lfind [options]
```

### Options

- `-c, --content`: Enable searching within file content (default: false).
- `-e, --expression`: Treat queries as Lucene query expressions (default: false). When enabled, all queries are interpreted as Lucene queries.
- `-h, --help`: Display help message and exit.
- `-m, --mimetypes=<mimeTypes>`: Specify MIME types to include in content search (comma-separated, e.g., pdf,doc,text). Default is no filter; used only in content search mode.
- `-p, --path=<directory>`: Specify the starting path for the search (default: current working directory).
- `-v, --verbose`: Enable verbose mode to print useful debugging information.

### Examples

**Note**: You can run the following examples (except piped input search) in both interactive mode (without providing a query) or direct command mode (providing a query).

#### Piped Input Search

You can combine `lfind` with other commands using pipes to search within the piped input.

Search for a term (substring) in the piped input (e.g., output of `ls` command):

```bash
ls | lfind "query"
```

Search for an expression in the piped input (e.g., contents of a file):

```bash
cat "test.txt" | lfind -e "one AND two"
```

#### File name search

Search within a specific directory path:

```bash
lfind -p "/path/to/directory" "query"
```

Search for a term (substring) in file names within the current directory:

```bash
lfind "query"
```

Search for an expression in file names:

```bash
lfind -e "one AND two"
```

```bash
lfind -e "one OR two"
```

```bash
lfind -e "one -two"
```

```bash
lfind -e "one AND two*"
```

Enable verbose mode to display additional information:

```bash
lfind -v "query"
```

#### File content search

Search for a term (substring) within file content:

```bash
lfind -c "query"
```

Search for an expression within file content:

```bash
lfind -ce "one AND two"
```

or 

```bash
lfind -c -e "one AND two"
```

Apply file type filters (e.g., PDF and text files) to your content searches:

```bash
lfind -c -m pdf,text "query"
```

or

```bash
lfind -c -m pdf -m text "query"
```

Feel free to experiment with different options and queries to leverage the full capabilities of lfind for searching file names and content efficiently.

## Contributing Guidelines

1. Bug Reports and Feature Requests
    - If you encounter a bug or have an idea for a new feature, please open an issue on GitHub to report it.
    - Provide detailed steps to reproduce the bug, including any relevant error messages or screenshots.
    - Clearly describe the expected behavior or propose your feature idea.
2. Code Contributions
   - Fork the repository and create a new branch for your changes.
   - Ensure your code follows the project's coding style and conventions.
   - Write clear, concise commit messages that explain the purpose of each change.
   - Open a pull request (PR) with a descriptive title and detailed description of your changes.
3. Documentation
   - Improve existing documentation or write new documentation for features or APIs.
   - Update the README file with any necessary information for users and contributors.

## Future Scope

While `lfind` currently provides powerful file system searching capabilities, there are several potential areas for future improvement and expansion:

- **Performance Optimizations**: Investigate methods to further optimize indexing and searching for larger file systems, enhancing speed and efficiency.

- **Additional File Types**: Expand file type support to include a broader range of formats, enabling comprehensive searching across various file types.

- **Enhanced Result Details**: Enhance result output by providing additional details such as matched lines within files or specific substrings matched during content-based searches.

- **File Monitoring and Precomputed Indexing**: Implement precomputed indexing and background file monitoring to maintain index consistency and accelerate search processes.

- **Custom Ranking and Scoring**: Customize the ranking and scoring functions to tailor search results based on specific file system retrieval tasks, such as boosting file matches over folder matches or prioritizing exact matches.

- **Query Optimization**: Optimize query processing internally to improve search performance, particularly for token-based searches using wildcard queries.

- **Improved Packaging**: Explore solutions to resolve compatibility issues with GraalVM, potentially downgrading the Lucene version or adopting other strategies for efficient packaging.

- **User Interface (UI)**: Develop a graphical user interface (GUI) version for users who prefer a visual interaction.

- **Advanced Search Filters**: Implement advanced search filters based on file attributes, metadata, or content properties to refine search results based on specific criteria.

- **Internationalization (i18n)**: Add support for different languages and localization.

Contributions and suggestions for these or other enhancements are welcome! Please feel free to open an issue or submit a pull request to discuss and collaborate on the future development of `lfind`.


## Acknowledgements

We extend our sincere gratitude to the following individuals and resources that have contributed to the development and improvement of `lfind`:

- **Open Source Libraries and Tools**:
    - [Apache Lucene](https://lucene.apache.org/): Powerful search library used as the core engine for `lfind`.
    - [picocli](https://picocli.info/): Command line parsing and validation library that enhances the user experience of `lfind`.
    - [Apache PDFBox](https://pdfbox.apache.org/): Library for working with PDF documents, providing essential features for PDF file handling in `lfind`.
    - [Apache Tika](https://tika.apache.org/): Toolkit for detecting and extracting metadata and text content from various file formats, enhancing content extraction capabilities in `lfind`.
    - [Apache POI](https://poi.apache.org/): Library for reading and writing Microsoft Office file formats (such as Word documents), extending file type support in `lfind`.


- **Development Tools**:
    - [ChatGPT](https://www.openai.com/gpt): Leveraged for generating this README and providing assistance in resolving coding issues.



## Contributors

- vva2
- mkr-peta
- bhavan-dondapati
- spchimmani

## License

- Licensed with Apache-2.0 license

