import { LitElement, html, css} from 'lit';
import { pages } from 'build-time-data';
import 'qwc/qwc-extension-link.js';

const NAME = "JasperReports";
export class QwcJasperReportsCard extends LitElement {

    static styles = css`
      .identity {
        display: flex;
        justify-content: flex-start;
      }

      .description {
        padding-bottom: 10px;
      }

      .logo {
        padding-bottom: 10px;
        margin-right: 5px;
      }

      .card-content {
        color: var(--lumo-contrast-90pct);
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        padding: 2px 2px;
        height: 100%;
      }

      .card-content slot {
        display: flex;
        flex-flow: column wrap;
        padding-top: 5px;
      }
    `;

    static properties = {
        description: {type: String}
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        return html`<div class="card-content" slot="content">
            <div class="identity">
                <div class="logo">
                    <img src="data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pg0KPCEtLSBVcGxvYWRlZCB0bzogU1ZHIFJlcG8sIHd3dy5zdmdyZXBvLmNvbSwgR2VuZXJhdG9yOiBTVkcgUmVwbyBNaXhlciBUb29scyAtLT4NCjxzdmcgaGVpZ2h0PSI4MDBweCIgd2lkdGg9IjgwMHB4IiB2ZXJzaW9uPSIxLjEiIGlkPSJMYXllcl8xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiANCgkgdmlld0JveD0iMCAwIDQ5MS41MiA0OTEuNTIiIHhtbDpzcGFjZT0icHJlc2VydmUiPg0KPHBhdGggc3R5bGU9ImZpbGw6I0ZDRDQ2MjsiIGQ9Ik0yMjkuNjQ0LDMyLjIzYy0xMjIuMzgsMC0yMjEuNTg3LDk5LjIwNy0yMjEuNTg3LDIyMS41ODdjMCw5MS41MjUsNTUuNDk3LDE3MC4wNzQsMTM0LjY2OCwyMDMuODcyDQoJbDg2LjkxOS0yMDMuODcyVjMyLjIzeiIvPg0KPHBhdGggc3R5bGU9ImZpbGw6I0Y2QzM1ODsiIGQ9Ik0yMTcuNjI0LDMyLjU2NXYyMjEuMjUybC04NC45MDksMTk5LjE1OWMzLjMxMywxLjYxNSw2LjYwOSwzLjI2LDEwLjAxMSw0LjcxMmw4Ni45MTktMjAzLjg3MlYzMi4yMw0KCUMyMjUuNjEsMzIuMjMsMjIxLjYwNSwzMi4zNTMsMjE3LjYyNCwzMi41NjV6Ii8+DQo8cGF0aCBzdHlsZT0iZmlsbDojM0VBNjlCOyIgZD0iTTQ2Ni4xOTksMjIxLjU4NmgxNy4yNjRDNDgzLjQ2Myw5OS4yMDcsMzg0LjI1NCwwLDI2MS44NzYsMHYxNC4yMjYNCglDMzczLjc0MSwxOC42NTUsNDYzLjM3MywxMDkuMzE2LDQ2Ni4xOTksMjIxLjU4NnoiLz4NCjxwYXRoIHN0eWxlPSJmaWxsOiMyRDkzQkE7IiBkPSJNMjM3LjE5Nyw0ODguNDA0Yy0yNy42MywwLTU0LjAwNC01LjMxOC03OC4yMy0xNC44OTJsLTAuMTI1LDAuMjkzDQoJYzI2LjY5LDExLjM5NCw1Ni4wNjQsMTcuNzE1LDg2LjkxOSwxNy43MTVjNjUuMjgyLDAsMTIzLjk1OC0yOC4yNDEsMTY0LjUxLTczLjE2bC04LjUyNS03LjY5MQ0KCUMzNjIuNjc1LDQ1OC4xMzQsMzAzLjQ3Myw0ODguNDA0LDIzNy4xOTcsNDg4LjQwNHoiLz4NCjxwYXRoIHN0eWxlPSJmaWxsOiNEMTUyNDE7IiBkPSJNNDU4LjE0MSwyNDUuNzZjMC4wNDYsMS44MTUsMC4xMzgsMy42MTksMC4xMzgsNS40NDdjMCw1MS4zNzUtMTguMTkxLDk4LjQ5Ny00OC40NzYsMTM1LjI4OQ0KCWw4LjUyNiw3LjY5MmMzNS40NjgtMzkuMjg4LDU3LjA3Ni05MS4zMzEsNTcuMDc2LTE0OC40MjdINDU4LjE0MXoiLz4NCjxwYXRoIHN0eWxlPSJmaWxsOiM0NEM0QTE7IiBkPSJNNDY2LjE5OSwyMjEuNTg2Yy0yLjgyNi0xMTIuMjctOTIuNDU4LTIwMi45MzItMjA0LjMyMy0yMDcuMzZ2MjA3LjM2TDQ2Ni4xOTksMjIxLjU4Ng0KCUw0NjYuMTk5LDIyMS41ODZ6Ii8+DQo8cGF0aCBzdHlsZT0iZmlsbDojMjdBMkRCOyIgZD0iTTQwMS43NDUsNDEwLjY2OUwyNDUuNzYsMjY5LjkzM2wtODYuNzk0LDIwMy41NzljMjQuMjI2LDkuNTc0LDUwLjYsMTQuODkyLDc4LjIzLDE0Ljg5Mg0KCUMzMDMuNDczLDQ4OC40MDQsMzYyLjY3NSw0NTguMTM0LDQwMS43NDUsNDEwLjY2OXoiLz4NCjxwYXRoIHN0eWxlPSJmaWxsOiNFNTYzNTM7IiBkPSJNNDA5LjgwMywzODYuNDk2YzMwLjI4Ni0zNi43OTIsNDguNDc2LTgzLjkxNCw0OC40NzYtMTM1LjI4OWMwLTEuODI4LTAuMDkyLTMuNjMyLTAuMTM4LTUuNDQ3DQoJSDI1My44MTdMNDA5LjgwMywzODYuNDk2eiIvPg0KPC9zdmc+"
                                       alt="${NAME}" 
                                       title="${NAME}"
                                       width="32" 
                                       height="32">
                </div>
                <div class="description">${this.description}</div>
            </div>
            ${this._renderCardLinks()}
        </div>
        `;
    }

    _renderCardLinks(){
        return html`${pages.map(page => html`
                            <qwc-extension-link slot="link"
                                extensionName="${NAME}"
                                iconName="${page.icon}"
                                displayName="${page.title}"
                                staticLabel="${page.staticLabel}"
                                dynamicLabel="${page.dynamicLabel}"
                                streamingLabel="${page.streamingLabel}"
                                path="${page.id}"
                                ?embed=${page.embed}
                                externalUrl="${page.metadata.externalUrl}"
                                webcomponent="${page.componentLink}" >
                            </qwc-extension-link>
                        `)}`;
    }

}
customElements.define('qwc-jasperreports-card', QwcJasperReportsCard);