<p align="center"><img src="https://raw.githubusercontent.com/Stirling-Tools/Stirling-PDF/main/docs/stirling.png" width="80" ></p>
<h1 align="center">EasySpdf</h1>



This is a robust, locally hosted web-based PDF manipulation tool using Docker **BASED ON  STIRLING-PDF**. It enables you to carry out various operations on PDF files, including splitting, merging, converting, reorganizing, adding images, rotating, compressing, and more. This locally hosted web application has evolved to encompass a comprehensive set of features, addressing all your PDF requirements.

Easyspdf is based on github open source project stirling-pdf (project github address is:https://github.com/Stirling-Tools/Stirling-PDF), and some of its functions are optimized and completed. The overall architecture of Easyspdf is basically the same as stirling-pdf.EasySpdf does not initiate any outbound calls for record-keeping or tracking purposes.

All files and PDFs exist either exclusively on the client side, reside in server memory only during task execution, or temporarily reside in a file solely for the execution of the task. Any file downloaded by the user will have been deleted from the server by that point.

![image-20240613134016569](C:\Users\sunhm3\AppData\Roaming\Typora\typora-user-images\image-20240613134016569.png)

## Features

- Dark mode support.
- Parallel file processing and downloads
- API for integration with external scripts
- Optional Login and Authentication support .
- Faster pdf to word processing.

## **PDF Features**

### **Page Operations**

- View and modify PDFs - View multi page PDFs with custom viewing sorting and searching. Plus on page edit features like annotate, draw and adding text and images. (Using PDF.js with Joxit and Liberation.Liberation fonts)
- Full interactive GUI for merging/splitting/rotating/moving PDFs and their pages.
- Merge multiple PDFs together into a single resultant file.
- Split PDFs into multiple files at specified page numbers or extract all pages as individual files.
- Reorganize PDF pages into different orders.
- Rotate PDFs in 90-degree increments.
- Remove pages.
- Multi-page layout (Format PDFs into a multi-paged page).
- Scale page contents size by set %.
- Adjust Contrast.
- Crop PDF.
- Auto Split PDF (With physically scanned page dividers).
- Extract page(s).
- Convert PDF to a single page.

### **Conversion Operations**

- Convert PDFs to and from images.
- Convert any common file to PDF (using LibreOffice).
- Convert PDF to Word/Powerpoint/Others (using LibreOffice).
- Convert HTML to PDF.
- URL to PDF.
- Markdown to PDF.

### **Security & Permissions**

- Add and remove passwords.
- Change/set PDF Permissions.
- Add watermark(s).
- Certify/sign PDFs.
- Sanitize PDFs.
- Auto-redact text.

### **Other Operations**

- Add/Generate/Write signatures.
- Repair PDFs.
- Detect and remove blank pages.
- Compare 2 PDFs and show differences in text.
- Add images to PDFs.
- Compress PDFs to decrease their filesize (Using OCRMyPDF).
- Extract images from PDF.
- Extract images from Scans.
- Add page numbers.
- Auto rename file by detecting PDF header text.
- OCR on PDF (Using OCRMyPDF).
- PDF/A conversion (Using OCRMyPDF).
- Edit metadata.
- Flatten PDFs.
- Get all information on a PDF to view or export as JSON.

For a overview of the tasks and the technology each uses please view [Endpoint-groups.md](https://github.com/Stirling-Tools/Stirling-PDF/blob/main/Endpoint-groups.md)
Demo of the app is available [here](https://stirlingpdf.io). username: demo, password: demo

## Technologies used

- Spring Boot + Thymeleaf
- [PDFBox](https://github.com/apache/pdfbox/tree/trunk)
- [LibreOffice](https://www.libreoffice.org/discover/libreoffice/) for advanced conversions
- [OcrMyPdf](https://github.com/ocrmypdf/OCRmyPDF)
- HTML, CSS, JavaScript
- Docker
- [PDF.js](https://github.com/mozilla/pdf.js)
- [PDF-LIB.js](https://github.com/Hopding/pdf-lib)



## Supported Languages

EasySpdf currently supports 32!

| Language                                    | Progress                               |
| ------------------------------------------- | -------------------------------------- |
| English (English) (en_GB)                   | ![100%](https://geps.dev/progress/100) |
| English (US) (en_US)                        | ![100%](https://geps.dev/progress/100) |
| Arabic (العربية) (ar_AR)                    | ![40%](https://geps.dev/progress/40)   |
| German (Deutsch) (de_DE)                    | ![99%](https://geps.dev/progress/99)   |
| French (Français) (fr_FR)                   | ![94%](https://geps.dev/progress/94)   |
| Spanish (Español) (es_ES)                   | ![94%](https://geps.dev/progress/94)   |
| Simplified Chinese (简体中文) (zh_CN)       | ![95%](https://geps.dev/progress/95)   |
| Traditional Chinese (繁體中文) (zh_TW)      | ![94%](https://geps.dev/progress/94)   |
| Catalan (Català) (ca_CA)                    | ![49%](https://geps.dev/progress/49)   |
| Italian (Italiano) (it_IT)                  | ![99%](https://geps.dev/progress/99)   |
| Swedish (Svenska) (sv_SE)                   | ![40%](https://geps.dev/progress/40)   |
| Polish (Polski) (pl_PL)                     | ![42%](https://geps.dev/progress/42)   |
| Romanian (Română) (ro_RO)                   | ![39%](https://geps.dev/progress/39)   |
| Korean (한국어) (ko_KR)                     | ![87%](https://geps.dev/progress/87)   |
| Portuguese Brazilian (Português) (pt_BR)    | ![61%](https://geps.dev/progress/61)   |
| Russian (Русский) (ru_RU)                   | ![86%](https://geps.dev/progress/86)   |
| Basque (Euskara) (eu_ES)                    | ![63%](https://geps.dev/progress/63)   |
| Japanese (日本語) (ja_JP)                   | ![87%](https://geps.dev/progress/87)   |
| Dutch (Nederlands) (nl_NL)                  | ![84%](https://geps.dev/progress/84)   |
| Greek (Ελληνικά) (el_GR)                    | ![85%](https://geps.dev/progress/85)   |
| Turkish (Türkçe) (tr_TR)                    | ![97%](https://geps.dev/progress/97)   |
| Indonesia (Bahasa Indonesia) (id_ID)        | ![78%](https://geps.dev/progress/78)   |
| Hindi (हिंदी) (hi_IN)                          | ![79%](https://geps.dev/progress/79)   |
| Hungarian (Magyar) (hu_HU)                  | ![77%](https://geps.dev/progress/77)   |
| Bulgarian (Български) (bg_BG)               | ![97%](https://geps.dev/progress/97)   |
| Sebian Latin alphabet (Srpski) (sr_LATN_RS) | ![80%](https://geps.dev/progress/80)   |
| Ukrainian (Українська) (uk_UA)              | ![86%](https://geps.dev/progress/86)   |
| Slovakian (Slovensky) (sk_SK)               | ![94%](https://geps.dev/progress/94)   |
| Czech (Česky) (cs_CZ)                       | ![93%](https://geps.dev/progress/93)   |
| Croatian (Hrvatski) (hr_HR)                 | ![98%](https://geps.dev/progress/98)   |
| Norwegian (Norsk) (no_NB)                   | ![98%](https://geps.dev/progress/98)   |

## FAQ

###  What are your planned features?

- Progress bar/Tracking
- Full custom logic pipelines to combine multiple operations together.
- Folder support with auto scanning to perform operations on
- Redact text (Via UI not just automated way)
- Add Forms
- Multi page layout (Stich PDF pages together) support x rows y columns and custom page sizing
- Fill forms manually or automatically

