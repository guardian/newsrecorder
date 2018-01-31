import React from 'react';
import DataTable from './DataTable.jsx';
//import ControlsBanner from './ControlsBanner.jsx';
import ErrorMessage from './ErrorMessage.jsx';

import axios from 'axios';

class MainPage extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            lastError: null,
            programmesList: [],
            channelsList: [],
            titleFilter: null,
            isLoading: false
        };

        this.channelFilterActivated = this.channelFilterActivated.bind(this);
        this.channelFilterDeactivated = this.channelFilterDeactivated.bind(this);
        this.titleSearchUpdated = this.titleSearchUpdated.bind(this);
    }

    channelFilterActivated(){

    }

    channelFilterDeactivated(){

    }

    loadProgrammeData(){
        const programmeArgs = this.state.titleFilter ? "?title="+this.state.titleFilter : "";

        this.setState({programmesList: [], isLoading: true});

        axios.get("/api/programmes" + programmeArgs).then(response=>{
            this.setState({programmesList: response.data, isLoading: false})
        }).catch(error=>this.setState({lastError: error, isLoading: false}));
    }

    componentWillMount(){
        this.setState({isLoading: true}, ()=>
            axios.get("/api/channels").then(response=>{
                const channelData = response.data.reduce((map,entry)=> {
                    map[entry.channelId] = entry;
                    return map;
                }, {});
                this.setState({channelsList: channelData}, ()=>this.loadProgrammeData());

            }).catch(error=>this.setState({lastError: error,isLoading: false}))
        )
    }

    loadingSpinner() {
        return this.state.isLoading ? <img src="/assets/images/loading.svg" style={{width: "16px"}} alt="loading"/> : "";
    }

    titleSearchUpdated(event){
        this.setState({titleFilter: event.target.value}, ()=>this.loadProgrammeData());
    }

    render(){
        return <div>
            <span>{this.loadingSpinner()}<ErrorMessage axiosError={this.state.lastError}/></span>
            <span>Search for title: <input onChange={this.titleSearchUpdated}/></span>
            <DataTable inputData={this.state.programmesList}
                       channelFilterDeactivated={this.channelFilterDeactivated}
                       channelsData={this.state.channelsList}/>
        </div>
    }
}

export default MainPage;